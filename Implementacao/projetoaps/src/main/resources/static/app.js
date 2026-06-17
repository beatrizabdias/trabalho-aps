let usuarioLogado = null;
let reposicoesAtivas = new Map();

async function fazerLogin() {
  const email = document.getElementById("emailLogin").value.trim();

  if (!email) {
    alert("Digite um e-mail.");
    return;
  }

  const resposta = await fetch(`/funcionarios/email?email=${encodeURIComponent(email)}`);

  if (!resposta.ok) {
    alert("Funcionário não encontrado.");
    return;
  }

  usuarioLogado = await resposta.json();

  document.getElementById("loginCard").style.display = "none";
  document.getElementById("sistema").style.display = "block";

  if (usuarioLogado.tipoFuncionario === 'ADMIN') {
    usuarioInfo.textContent =
    `${usuarioLogado.nome} | ADMIN | Acesso Global`;
  }

  document.getElementById("usuarioInfo").textContent =
    `${usuarioLogado.nome} | ${usuarioLogado.email} | ${usuarioLogado.loja?.nome ?? "Sem loja"}`;

  aplicarPermissoes();
  await carregarTudo();

  setInterval(() => {
    if (usuarioLogado) {
      carregarDadosDinamicos();
    }
  }, 2000);
}

function aplicarPermissoes() {

  const tipo =
    usuarioLogado.tipoFuncionario ||
    usuarioLogado.tipo_funcionario ||
    "";

  const areaGestao =
    document.getElementById("areaGestao");

  if (!areaGestao) {
    console.error("areaGestao não encontrada");
    return;
  }

  areaGestao.style.display =
    (tipo === "GERENTE" || tipo === "ADMIN")
      ? "block"
      : "none";
}

function sair() {
  usuarioLogado = null;

  document.getElementById("emailLogin").value = "";
  document.getElementById("loginCard").style.display = "block";
  document.getElementById("sistema").style.display = "none";
}

async function carregarProdutos() {
  const resposta = await fetch("/produtos");
  const produtos = await resposta.json();

  const select = document.getElementById("produtoSelect");
  select.innerHTML = "";

  produtos.forEach(produto => {
    const option = document.createElement("option");
    option.value = produto.id;
    option.textContent = produto.nome;
    select.appendChild(option);
  });
}

async function carregarLojas() {
  const resposta = await fetch("/lojas");
  const lojas = await resposta.json();

  const select = document.getElementById("lojaSelect");
  select.innerHTML = "";

  lojas.forEach(loja => {
    const option = document.createElement("option");
    option.value = loja.id;
    option.textContent = loja.nome;
    select.appendChild(option);
  });

  if (usuarioLogado?.loja?.id) {
    select.value = usuarioLogado.loja.id;
    select.disabled = true;
  }
}

async function registrarVenda() {
  const produtoId = document.getElementById("produtoSelect").value;
  const lojaId = document.getElementById("lojaSelect").value;
  const quantidade = document.getElementById("quantidade").value;
  const produtoNome = document.querySelector(`#produtoSelect option[value="${produtoId}"]`)?.textContent || "Produto";
  const lojaNome = document.querySelector(`#lojaSelect option[value="${lojaId}"]`)?.textContent || "Loja";

  atualizarStatusReposicao(
    `Analisando ${produtoNome} na ${lojaNome} para reposição automática...`,
    "processando"
  );

  const resposta = await fetch(
    `/vendas?produtoId=${produtoId}&lojaId=${lojaId}&quantidade=${quantidade}`,
    { method: "POST" }
  );

  if (resposta.ok) {
    const retorno = await resposta.json();
    const chave = criarChaveReposicao(produtoId, lojaId);

    if (retorno.tipoReposicao && retorno.tipoReposicao !== "SEM_ACAO") {
      reposicoesAtivas.set(chave, {
        produtoId: Number(produtoId),
        lojaId: Number(lojaId),
        produtoNome,
        lojaNome,
        tipoReposicao: retorno.tipoReposicao,
        mensagem: retorno.detalheReposicao || retorno.mensagem,
        status: "em_andamento",
      });

      atualizarStatusReposicao(
        `${produtoNome} na ${lojaNome}: ${retorno.mensagem} ${retorno.detalheReposicao || ""}`.trim(),
        "analise"
      );
    } else {
      reposicoesAtivas.delete(chave);
      atualizarStatusReposicao(
        `${produtoNome} na ${lojaNome}: ${retorno.mensagem || "Venda registrada com sucesso."}`,
        "ok"
      );
    }

    await carregarTudo();
  } else {
    atualizarStatusReposicao(`Não foi possível registrar ${produtoNome} na ${lojaNome}.`, "erro");
    alert("Erro ao registrar venda.");
  }
}

async function carregarEstoques() {
  const resposta = await fetch("/estoques");
  let estoques = await resposta.json();

  if (usuarioLogado?.loja?.id) {
    estoques = estoques.filter(
      estoque => estoque.loja.id === usuarioLogado.loja.id
    );
  }

  const tabela = document.getElementById("tabelaEstoques");
  tabela.innerHTML = "";

  estoques.forEach(estoque => {
        const reposicao = obterStatusReposicaoLinha(estoque);

    tabela.innerHTML += `
      <tr>
        <td>${estoque.produto.nome}</td>
        <td>${estoque.loja.nome}</td>
        <td>${estoque.quantidade}</td>
        <td><span class="estado-badge ${getClasseEstado(estoque.estado)}">${formatarEstado(estoque.estado)}</span></td>
            <td>${reposicao}</td>
      </tr>
    `;
  });

      verificarConclusaoReposicao(estoques);
}

function formatarEstado(estado) {
  if (!estado) return "⚪ Sem status";
  
  // Remove acentos e converte para maiúsculas para comparar
  const e = estado.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toUpperCase();

  if (e.includes("DISPONIVEL") || e.includes("NORMAL")) return "🟢 Disponível";
  if (e.includes("ALERTA")) return "🟡 Alerta";
  if (e.includes("CRITICO")) return "🔴 Crítico";
  if (e.includes("ESGOTADO")) return "⚫ Esgotado";

  return "⚪ " + estado;
}

function getClasseEstado(estado) {
  if (!estado) return "estado-neutro";

  const e = estado.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toUpperCase();

  if (e.includes("DISPONIVEL") || e.includes("NORMAL")) return "estado-disponivel";
  if (e.includes("ALERTA")) return "estado-alerta";
  if (e.includes("CRITICO")) return "estado-critico";
  if (e.includes("ESGOTADO")) return "estado-esgotado";

  return "estado-neutro";
}

function atualizarStatusReposicao(texto, tipo) {
  const caixa = document.getElementById("statusReposicao");

  if (!caixa) return;

  caixa.textContent = texto;
  caixa.className = `status-reposicao status-${tipo}`;
}

function criarChaveReposicao(produtoId, lojaId) {
  return `${produtoId}:${lojaId}`;
}

function obterStatusReposicaoLinha(estoque) {
  const estadoNormalizado = normalizarTexto(estoque.estado);
  const chave = criarChaveReposicao(estoque.produto?.id, estoque.loja?.id);
  const reposicao = reposicoesAtivas.get(chave);

  if (reposicao) {
    const acao = reposicao.tipoReposicao === "TRANSFERENCIA" ? "Transferência" : "Compra";
    const destino = reposicao.tipoReposicao === "TRANSFERENCIA"
      ? ` com origem em ${reposicao.mensagem?.includes("origem") ? "outra loja" : "estoque disponível"}`
      : "";

    return `<span class="reposicao-label reposicao-andamento">${acao} automática em andamento: ${estoque.produto.nome} / ${estoque.loja.nome}${destino}</span>`;
  }

  if (estadoNormalizado.includes("CRITICO")) {
    return `<span class="reposicao-label reposicao-critica">Crítico: aguardando transferência ou compra para ${estoque.produto.nome} / ${estoque.loja.nome}</span>`;
  }

  if (estadoNormalizado.includes("ESGOTADO")) {
    return `<span class="reposicao-label reposicao-esgotada">Esgotado: compra automática para ${estoque.produto.nome} / ${estoque.loja.nome}</span>`;
  }

  return `<span class="reposicao-label reposicao-ok">Sem reposição pendente</span>`;
}

function normalizarTexto(texto) {
  return (texto || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toUpperCase();
}

function verificarConclusaoReposicao(estoques) {
  if (!reposicoesAtivas.size) return;

  for (const [chave, reposicao] of reposicoesAtivas.entries()) {
    const estoqueAlvo = estoques.find(
      estoque =>
        estoque.produto?.id === reposicao.produtoId &&
        estoque.loja?.id === reposicao.lojaId
    );

    if (!estoqueAlvo) continue;

    const estadoNormalizado = normalizarTexto(estoqueAlvo.estado);
    const estadoAtual = formatarEstado(estoqueAlvo.estado);

    if (estadoNormalizado.includes("CRITICO") || estadoNormalizado.includes("ESGOTADO")) {
      reposicoesAtivas.set(chave, {
        ...reposicao,
        status: "em_andamento",
      });
      continue;
    }

    atualizarStatusReposicao(
      `${reposicao.produtoNome} na ${reposicao.lojaNome}: reposição concluída automaticamente. Novo estado: ${estadoAtual}.`,
      "ok"
    );

    reposicoesAtivas.delete(chave);
  }
}

function formatarData(data) {
    const d = new Date(data);

    return d.toLocaleDateString("pt-BR")
        + " "
        + d.toLocaleTimeString(
            "pt-BR",
            {
                hour: "2-digit",
                minute: "2-digit"
            }
        );
}

async function carregarTransferencias() {

  const resposta = await fetch("/transferencias");
  let transferencias = await resposta.json();

  const tipo =
    usuarioLogado.tipoFuncionario ||
    usuarioLogado.tipo_funcionario;

  // Gerente vê apenas sua loja
  if (tipo === "GERENTE" && usuarioLogado.loja) {
    transferencias = transferencias.filter(
      t =>
        t.lojaOrigem.id === usuarioLogado.loja.id ||
        t.lojaDestino.id === usuarioLogado.loja.id
    );
  }

  const tabela =
    document.getElementById("tabelaTransferencias");

  tabela.innerHTML = "";

  transferencias.forEach(t => {

    tabela.innerHTML += `
      <tr>
        <td>${t.produto.nome}</td>
        <td>${t.lojaOrigem.nome} -> ${t.lojaDestino.nome}</td>
        <td>${t.quantidade}</td>
        <td>${formatarData(t.dataTransferencia)}</td>
      </tr>
    `;
  });
}

async function carregarOrdensCompra() {
  const resposta = await fetch("/ordens-compra");
  const ordens = await resposta.json();

  const tabela = document.getElementById("tabelaOrdens");
  tabela.innerHTML = "";

  ordens.forEach(o => {
    const data = new Date(o.dataCriacao);
    const dataFormatada = formatarData(data);

    tabela.innerHTML += `
      <tr>
        <td>${o.produto.nome}</td>
        <td>${o.loja.nome}</td>
        <td>${o.fornecedor ? o.fornecedor.razaoSocial : "Sem fornecedor"}</td>
        <td>${o.quantidade}</td>
        <td>${o.status}</td>
        <td>${dataFormatada}</td>
      </tr>
    `;
  });
}

async function carregarTudo() {
  // Dados comuns a todos
  await carregarProdutos();
  await carregarLojas();
  await carregarEstoques();

  // Verifica o tipo de usuário para carregar dados sensíveis
  const tipo = usuarioLogado?.tipoFuncionario || usuarioLogado?.tipo_funcionario;
  console.log(usuarioLogado);

  const areaGestao = document.getElementById("areaGestao");

  if (tipo === "GERENTE" || tipo === "ADMIN") {
    areaGestao.style.display = "block";
    await carregarTransferencias();
    await carregarOrdensCompra();
  } else {
    areaGestao.style.display = "none";
  }
}

async function carregarDadosDinamicos() {
    await carregarEstoques();

    const tipo =
        usuarioLogado?.tipoFuncionario ||
        usuarioLogado?.tipo_funcionario;

    if (tipo === "GERENTE" || tipo === "ADMIN") {
        await carregarTransferencias();
        await carregarOrdensCompra();
    }
}