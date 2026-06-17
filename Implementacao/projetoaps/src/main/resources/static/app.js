let usuarioLogado = null;
let reposicoesAtivas = new Map();
let comprasManuaisAtivas = new Map();
let timerMensagemReposicao = null;
let timerMensagemCompraManual = null;

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

  // Captura o tipo de funcionário de qualquer uma das duas chaves possíveis (camelCase ou snake_case)
  const tipo = ( usuarioLogado.tipo_funcionario || "").toUpperCase().trim();

  const infoElemento = document.getElementById("usuarioInfo");

  // Agora compara o tipo normalizado em maiúsculo
  if (tipo === 'ADMIN') {
    infoElemento.textContent = `${usuarioLogado.nome} | ADMIN | Acesso Global`;
  } else {
    infoElemento.textContent = `${usuarioLogado.nome} | ${usuarioLogado.email} | ${usuarioLogado.loja?.nome ?? "Sem loja"}`;
  }

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

  document.getElementById("cardCompra").style.display =
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

  const vendaSelect = document.getElementById("produtoSelect");
  const compraSelect = document.getElementById("produtoCompraSelect");

  vendaSelect.innerHTML = "";
  compraSelect.innerHTML = "";

  produtos.forEach(produto => {
    const optionVenda = document.createElement("option");
    optionVenda.value = produto.id;
    optionVenda.textContent = produto.nome;
    vendaSelect.appendChild(optionVenda);

    const optionCompra = document.createElement("option");
    optionCompra.value = produto.id;
    optionCompra.textContent = produto.nome;
    compraSelect.appendChild(optionCompra);
  });
}

async function carregarLojas() {
  const resposta = await fetch("/lojas");
  const lojas = await resposta.json();

  const vendaSelect = document.getElementById("lojaSelect");
  const compraSelect = document.getElementById("lojaCompraSelect");

  vendaSelect.innerHTML = "";
  compraSelect.innerHTML = "";

  lojas.forEach(loja => {
    const optionVenda = document.createElement("option");
    optionVenda.value = loja.id;
    optionVenda.textContent = loja.nome;
    vendaSelect.appendChild(optionVenda);

    const optionCompra = document.createElement("option");
    optionCompra.value = loja.id;
    optionCompra.textContent = loja.nome;
    compraSelect.appendChild(optionCompra);
  });

  if (usuarioLogado?.loja?.id) {
    vendaSelect.value = usuarioLogado.loja.id;
    vendaSelect.disabled = true;
    compraSelect.value = usuarioLogado.loja.id;
    compraSelect.disabled = true;
  }
}

async function registrarVenda() {
  const produtoId = document.getElementById("produtoSelect").value;
  const lojaId = document.getElementById("lojaSelect").value;
  const quantidade = Number(document.getElementById("quantidade").value);
  const produtoNome = document.querySelector(`#produtoSelect option[value="${produtoId}"]`)?.textContent || "Produto";
  const lojaNome = document.querySelector(`#lojaSelect option[value="${lojaId}"]`)?.textContent || "Loja";

  if (!Number.isInteger(quantidade) || quantidade <= 0) {
    atualizarStatusReposicao(
      `Quantidade inválida para ${produtoNome} na ${lojaNome}. Use um valor maior que zero.`,
      "erro"
    );
    alert("A quantidade precisa ser um número inteiro maior que zero.");
    return;
  }

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
      limparTimerReposicao(chave);
      reposicoesAtivas.set(chave, {
        produtoId: Number(produtoId),
        lojaId: Number(lojaId),
        produtoNome,
        lojaNome,
        tipoReposicao: retorno.tipoReposicao,
        mensagem: retorno.detalheReposicao || retorno.mensagem,
        status: "em_andamento",
        textoConclusao: null,
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

async function solicitarCompra() {

  const produtoId =
    document.getElementById("produtoCompraSelect").value;

  const lojaId =
    document.getElementById("lojaCompraSelect").value;

  const quantidade = Number(document.getElementById("quantidadeCompra").value);
  const produtoNome = document.querySelector(`#produtoCompraSelect option[value="${produtoId}"]`)?.textContent || "Produto";
  const lojaNome = document.querySelector(`#lojaCompraSelect option[value="${lojaId}"]`)?.textContent || "Loja";

  if (!Number.isInteger(quantidade) || quantidade <= 0) {
    atualizarStatusCompra(
      `Quantidade inválida para ${produtoNome} na ${lojaNome}. Use um valor maior que zero.`,
      "erro"
    );
    alert("A quantidade precisa ser um número inteiro maior que zero.");
    return;
  }

  atualizarStatusCompra(
    `Solicitando compra manual de ${produtoNome} para ${lojaNome}...`,
    "processando"
  );

  const resposta = await fetch(
    `/ordens-compra/manual?produtoId=${produtoId}&lojaId=${lojaId}&quantidade=${quantidade}`,
    { method: "POST" }
  );

  if (resposta.ok) {
    const chave = criarChaveReposicao(produtoId, lojaId);
    
    comprasManuaisAtivas.set(chave, {
      produtoId: Number(produtoId),
      lojaId: Number(lojaId),
      produtoNome,
      lojaNome,
      status: "em_andamento",
      timerId: null,
    });
    
    atualizarStatusCompra(`Compra manual de ${produtoNome} para ${lojaNome} solicitada com sucesso.`, "ok");
    agendarLimpezaCompraManual(chave);
    await carregarTudo();
  } else {
    const erro = await resposta.text();
    atualizarStatusCompra(`Erro ao solicitar compra manual: ${erro}`, "erro");
    alert("Erro ao solicitar compra.");
  }
}

function atualizarStatusCompra(texto, tipo) {
  const caixa = document.getElementById("statusCompraManual");

  if (!caixa) return;

  if (timerMensagemCompraManual) {
    clearTimeout(timerMensagemCompraManual);
    timerMensagemCompraManual = null;
  }

  caixa.textContent = texto;
  caixa.className = `status-reposicao status-${tipo}`;

  if (tipo !== "erro") {
    timerMensagemCompraManual = setTimeout(() => {
      const elemento = document.getElementById("statusCompraManual");

      if (!elemento) return;

      elemento.textContent = "Use esta opção para registrar uma ordem de compra manual que será enviada ao fornecedor.";
      elemento.className = "status-reposicao status-neutro";
    }, 8000);
  }
}

function formatarStatus(status) {

  switch(status) {
    case "PENDENTE":
      return "🟡 Pedido enviado";

    case "EM_PROCESSAMENTO":
      return "🚚 Em transporte";

    case "CONCLUIDA":
      return "✅ Recebido";

    default:
      return status;
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

  if (timerMensagemReposicao) {
    clearTimeout(timerMensagemReposicao);
    timerMensagemReposicao = null;
  }

  caixa.textContent = texto;
  caixa.className = `status-reposicao status-${tipo}`;

  if (tipo !== "erro") {
    timerMensagemReposicao = setTimeout(() => {
      const elemento = document.getElementById("statusReposicao");

      if (!elemento) return;

      elemento.textContent = "O sistema acompanha automaticamente o estoque após a venda.";
      elemento.className = "status-reposicao status-neutro";
    }, 8000);
  }
}

function criarChaveReposicao(produtoId, lojaId) {
  return `${produtoId}:${lojaId}`;
}

function obterStatusReposicaoLinha(estoque) {
  const estadoNormalizado = normalizarTexto(estoque.estado);
  const chave = criarChaveReposicao(estoque.produto?.id, estoque.loja?.id);
  const reposicao = reposicoesAtivas.get(chave);
  const compraManual = comprasManuaisAtivas.get(chave);

  // Verificar se há compra manual em andamento
  if (compraManual) {
    if (compraManual.status === "em_andamento") {
      return `<span class="reposicao-badge reposicao-andamento">📋 Compra manual solicitada</span>`;
    }
    if (compraManual.status === "concluida") {
      return `<span class="reposicao-badge reposicao-finalizada">✅ Compra manual concluída</span>`;
    }
  }

  if (reposicao) {
    if (reposicao.status === "concluida") {
      return `<span class="reposicao-badge reposicao-finalizada">✅ Concluída</span>`;
    }

    const acao = reposicao.tipoReposicao === "TRANSFERENCIA" ? "Transferência" : "Compra";
    return `<span class="reposicao-badge reposicao-andamento">🚚 ${acao} automática</span>`;
  }

  if (estadoNormalizado.includes("CRITICO")) {
    return `<span class="reposicao-badge reposicao-critica">⚠️ Aguardando ação</span>`;
  }

  if (estadoNormalizado.includes("ESGOTADO")) {
    return `<span class="reposicao-badge reposicao-esgotada">🚨 Compra automática</span>`;
  }

  return `<span class="reposicao-badge reposicao-ok">Sem pendências</span>`;
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

    if (reposicao.status === "concluida") {
      continue;
    }

    const estadoNormalizado = normalizarTexto(estoqueAlvo.estado);
    const estadoAtual = formatarEstado(estoqueAlvo.estado);

    if (estadoNormalizado.includes("CRITICO") || estadoNormalizado.includes("ESGOTADO")) {
      reposicoesAtivas.set(chave, {
        ...reposicao,
        status: "em_andamento",
      });
      continue;
    }

    const textoConclusao = `${reposicao.produtoNome} na ${reposicao.lojaNome}: reposição concluída automaticamente. Novo estado: ${estadoAtual}.`;

    reposicoesAtivas.set(chave, {
      ...reposicao,
      status: "concluida",
      textoConclusao,
    });

    atualizarStatusReposicao(textoConclusao, "ok");
    agendarLimpezaReposicao(chave);
  }
}

function limparTimerReposicao(chave) {
  const reposicao = reposicoesAtivas.get(chave);

  if (reposicao?.timerId) {
    clearTimeout(reposicao.timerId);
  }
}

function agendarLimpezaReposicao(chave) {
  limparTimerReposicao(chave);

  const timerId = setTimeout(() => {
    reposicoesAtivas.delete(chave);

    if (usuarioLogado) {
      carregarDadosDinamicos();
    }
  }, 7000);

  const reposicaoAtual = reposicoesAtivas.get(chave);

  if (reposicaoAtual) {
    reposicoesAtivas.set(chave, {
      ...reposicaoAtual,
      timerId,
    });
  }
}

function limparTimerCompraManual(chave) {
  const compra = comprasManuaisAtivas.get(chave);

  if (compra?.timerId) {
    clearTimeout(compra.timerId);
  }
}

function agendarLimpezaCompraManual(chave) {
  limparTimerCompraManual(chave);

  const timerId = setTimeout(() => {
    comprasManuaisAtivas.delete(chave);

    if (usuarioLogado) {
      carregarDadosDinamicos();
    }
  }, 7000);

  const compraAtual = comprasManuaisAtivas.get(chave);

  if (compraAtual) {
    comprasManuaisAtivas.set(chave, {
      ...compraAtual,
      timerId,
    });
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

async function carregarVendas() {
  const resposta = await fetch("/vendas");

  if (!resposta.ok) {
    console.error("Erro ao carregar vendas:", resposta.status, await resposta.text());
    return;
  }

  let vendas = await resposta.json();
  if (!Array.isArray(vendas)) {
    console.warn("Resposta de vendas não é uma lista:", vendas);
    vendas = [];
  }

  const tipo =
    usuarioLogado.tipoFuncionario ||
    usuarioLogado.tipo_funcionario;

  if (tipo === "GERENTE" && usuarioLogado.loja) {
    vendas = vendas.filter(v => v.loja && v.loja.id === usuarioLogado.loja.id);
  }

  const tabela = document.getElementById("tabelaVendas");
  if (!tabela) return;
  tabela.innerHTML = "";

  vendas.forEach(v => {
    const dataFormatada = formatarData(v.dataVenda || v.dataCriacao || new Date());

    tabela.innerHTML += `
      <tr>
        <td>${v.produto?.nome ?? "Sem produto"}</td>
        <td>${v.loja?.nome ?? "Sem loja"}</td>
        <td>${v.quantidade ?? ""}</td>
        <td>${dataFormatada}</td>
      </tr>
    `;
  });
}

async function carregarOrdensCompra() {
  const resposta = await fetch("/ordens-compra");
  let ordens = await resposta.json();

  const tipo = (usuarioLogado?.tipo_funcionario || "").toUpperCase().trim();

  // filtra para exibir apenas as ordens cujo ID da loja case com a loja do gerente
  if (tipo === "GERENTE" && usuarioLogado?.loja?.id) {
    ordens = ordens.filter(
      o => o.loja?.id === usuarioLogado.loja.id
    );
  }

  const tabela = document.getElementById("tabelaOrdens");
  tabela.innerHTML = "";

  ordens.forEach(o => {
    const data = new Date(o.dataCriacao);
    const dataFormatada = formatarData(data);

    tabela.innerHTML += `
      <tr>
        <td>${o.produto?.nome ?? "Sem produto"}</td>
        <td>${o.loja?.nome ?? "Sem loja"}</td>
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

  const areaGestao = document.getElementById("areaGestao");

  if (tipo === "GERENTE" || tipo === "ADMIN") {
    areaGestao.style.display = "block";
    await carregarTransferencias();
    await carregarOrdensCompra();
    await carregarVendas();
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
        await carregarVendas();
    }
}

function baixarArquivo(nomeArquivo, conteudo) {
  const blob = new Blob([conteudo], { type: "text/plain" });
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = nomeArquivo;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

async function gerarRelatorioTransferencias() {
  const resposta = await fetch("/transferencias");
  let transferencias = await resposta.json();

  const tipo = (usuarioLogado.tipoFuncionario || usuarioLogado.tipo_funcionario || "").toUpperCase().trim();

  if (tipo === "GERENTE" && usuarioLogado.loja) {
    transferencias = transferencias.filter(
      t => t.lojaOrigem.id === usuarioLogado.loja.id || t.lojaDestino.id === usuarioLogado.loja.id
    );
  }

  let totalItens = 0;
  const produtos = {};

  transferencias.forEach(t => {
    totalItens += t.quantidade;
    if (!produtos[t.produto.nome]) {
      produtos[t.produto.nome] = 0;
    }
    produtos[t.produto.nome] += t.quantidade;
  });

  let produtoMaisTransferido = "Nenhum";
  let maiorQuantidade = 0;

  for (const produto in produtos) {
    if (produtos[produto] > maiorQuantidade) {
      maiorQuantidade = produtos[produto];
      produtoMaisTransferido = produto;
    }
  }

  let texto = `==========================================================
MERCADO CARIOCADA
RELATÓRIO DE TRANSFERÊNCIAS
==========================================================

Data de Geração: ${new Date().toLocaleString("pt-BR")}
Gerado por: ${usuarioLogado.nome}
Perfil: ${tipo}
Loja: ${usuarioLogado.loja?.nome || "Todas"}

----------------------------------------------------------
Total de Transferências: ${transferencias.length}
Total de Itens Movimentados: ${totalItens}
Produto Mais Transferido: ${produtoMaisTransferido}
Quantidade Movimentada: ${maiorQuantidade}

==========================================================
DETALHAMENTO
==========================================================\n`;

  transferencias.forEach(t => {
    texto += `
Produto: ${t.produto.nome}
Origem: ${t.lojaOrigem.nome}
Destino: ${t.lojaDestino.nome}
Quantidade: ${t.quantidade}
Data: ${formatarData(t.dataTransferencia)}
----------------------------------------------------------\n`;
  });

  gerarPDF("Relatorio_Transferencias.pdf", texto);
}

async function gerarRelatorioVendas() {
  const resposta = await fetch("/vendas");
  let vendas = await resposta.json();

  const tipo = (usuarioLogado.tipoFuncionario || usuarioLogado.tipo_funcionario || "").toUpperCase().trim();

  if (tipo === "GERENTE" && usuarioLogado.loja) {
    vendas = vendas.filter(v => v.loja && v.loja.id === usuarioLogado.loja.id);
  }

  let totalItens = 0;
  const produtos = {};

  vendas.forEach(v => {
    totalItens += v.quantidade;
    if (!produtos[v.produto.nome]) produtos[v.produto.nome] = 0;
    produtos[v.produto.nome] += v.quantidade;
  });

  let produtoMaisVendido = "Nenhum";
  let maiorQuantidade = 0;

  for (const produto in produtos) {
    if (produtos[produto] > maiorQuantidade) {
      maiorQuantidade = produtos[produto];
      produtoMaisVendido = produto;
    }
  }

  let texto = `==========================================================\nMERCADO CARIOCADA\nRELATÓRIO DE VENDAS\n==========================================================\n\nData de Geração: ${new Date().toLocaleString("pt-BR")}\nGerado por: ${usuarioLogado.nome}\nPerfil: ${tipo}\nLoja: ${usuarioLogado.loja?.nome || "Todas"}\n\n----------------------------------------------------------\nTotal de Vendas: ${vendas.length}\nTotal de Itens Vendidos: ${totalItens}\nProduto Mais Vendido: ${produtoMaisVendido}\nQuantidade Vendida: ${maiorQuantidade}\n\n==========================================================\nDETALHAMENTO\n==========================================================\n`;

  vendas.forEach(v => {
    texto += `\nProduto: ${v.produto.nome}\nLoja: ${v.loja.nome}\nQuantidade: ${v.quantidade}\nData: ${formatarData(v.dataVenda)}\n----------------------------------------------------------\n`;
  });

  gerarPDF("Relatorio_Vendas.pdf", texto);
}

async function gerarRelatorioCompras() {
  const resposta = await fetch("/ordens-compra");
  let ordens = await resposta.json();

  const tipo = (usuarioLogado.tipoFuncionario || usuarioLogado.tipo_funcionario || "").toUpperCase().trim();

  if (tipo === "GERENTE" && usuarioLogado.loja) {
    ordens = ordens.filter(o => o.loja.id === usuarioLogado.loja.id);
  }

  let totalItens = 0;
  let valorTotal = 0;
  const fornecedores = {};
  const produtos = {};

  ordens.forEach(o => {
    totalItens += o.quantidade;
    if (o.produto.valorCompra) {
      valorTotal += o.quantidade * o.produto.valorCompra;
    }
    if (o.fornecedor) {
      if (!fornecedores[o.fornecedor.razaoSocial]) {
        fornecedores[o.fornecedor.razaoSocial] = 0;
      }
      fornecedores[o.fornecedor.razaoSocial] += o.quantidade;
    }
    if (!produtos[o.produto.nome]) {
      produtos[o.produto.nome] = 0;
    }
    produtos[o.produto.nome] += o.quantidade;
  });

  let texto = `==========================================================
MERCADO CARIOCADA
RELATÓRIO DE ORDENS DE COMPRA
==========================================================

Data de Geração: ${new Date().toLocaleString("pt-BR")}
Gerado por: ${usuarioLogado.nome}
Perfil: ${tipo}
Loja: ${usuarioLogado.loja?.nome || "Todas"}

----------------------------------------------------------
Total de Ordens: ${ordens.length}
Quantidade Total Comprada: ${totalItens}
Valor Estimado Comprado: R$ ${valorTotal.toFixed(2)}

==========================================================
FORNECEDORES
==========================================================\n`;

  for (const fornecedor in fornecedores) {
    texto += `${fornecedor}\nQuantidade Fornecida: ${fornecedores[fornecedor]}\n\n`;
  }

  texto += `==========================================================
DETALHAMENTO DAS ORDENS
==========================================================\n`;

  ordens.forEach(o => {
    texto += `
Produto: ${o.produto.nome}
Fornecedor: ${o.fornecedor ? o.fornecedor.razaoSocial : "Não informado"}
Quantidade: ${o.quantidade}
Valor Unitário: R$ ${o.produto.valorCompra || 0}
Valor Total: R$ ${(o.quantidade * (o.produto.valorCompra || 0)).toFixed(2)}
Status: ${o.status}
Loja: ${o.loja.nome}
Data: ${formatarData(o.dataCriacao)}
----------------------------------------------------------\n`;
  });

  gerarPDF("Relatorio_Compras.pdf", texto);
}

function gerarPDF(nomeArquivo, texto) {
  const { jsPDF } = window.jspdf;
  const doc = new jsPDF();

  doc.setFont("courier", "normal");
  doc.setFontSize(10);

  // Divide o texto gigante em linhas aceitáveis para a largura da página A4
  const linhas = doc.splitTextToSize(texto, 180);
  
  let y = 15; // Margem superior inicial
  const margemInferiorMax = 280; // Limite antes de pular a página

  linhas.forEach(linha => {
    // Se a linha atual passar do limite inferior da folha, cria uma nova página
    if (y > margemInferiorMax) {
      doc.addPage();
      y = 15; // Reseta o topo na nova página
    }
    
    doc.text(linha, 12, y);
    y += 6; // Altura da linha (espaçamento entre uma linha e outra)
  });

  doc.save(nomeArquivo);
}

async function gerarRelatorioVendas() {
  const resposta = await fetch("/vendas");
  let vendas = await resposta.json();

  const tipo = (usuarioLogado.tipoFuncionario || usuarioLogado.tipo_funcionario || "").toUpperCase().trim();

  if (tipo === "GERENTE" && usuarioLogado.loja) {
    vendas = vendas.filter(v => v.loja.id === usuarioLogado.loja.id);
  }

  let totalItens = 0;
  let faturamentoEstimado = 0;

  vendas.forEach(v => {
    totalItens += v.quantidade;
    // Se houver valor de venda no seu objeto de produto
    if (v.produto?.valorVenda) {
      faturamentoEstimado += v.quantidade * v.produto.valorVenda;
    }
  });

  let texto = `==========================================================
MERCADO CARIOCADA
RELATÓRIO HISTÓRICO DE VENDAS
==========================================================

Data de Geração: ${new Date().toLocaleString("pt-BR")}
Gerado por: ${usuarioLogado.nome}
Perfil: ${tipo}
Loja: ${usuarioLogado.loja?.nome || "Todas"}

----------------------------------------------------------
Total de Vendas Registradas: ${vendas.length}
Total de Itens Vendidos: ${totalItens}
Faturamento Estimado: R$ ${faturamentoEstimado.toFixed(2)}

==========================================================
DETALHAMENTO DAS VENDAS
==========================================================\n`;

  vendas.forEach(v => {
    texto += `
Produto: ${v.produto?.nome ?? "Não informado"}
Loja: ${v.loja?.nome ?? "Não informada"}
Quantidade: ${v.quantidade}
Data: ${formatarData(v.dataVenda || v.dataCriacao)}
----------------------------------------------------------\n`;
  });

  gerarPDF("Relatorio_Vendas.pdf", texto);
}