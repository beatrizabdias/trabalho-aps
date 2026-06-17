let usuarioLogado = null;

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
  carregarTudo();
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

  const resposta = await fetch(
    `/vendas?produtoId=${produtoId}&lojaId=${lojaId}&quantidade=${quantidade}`,
    { method: "POST" }
  );

  if (resposta.ok) {
    alert("Venda registrada com sucesso!");
    carregarTudo();
  } else {
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
    tabela.innerHTML += `
      <tr>
        <td>${estoque.produto.nome}</td>
        <td>${estoque.loja.nome}</td>
        <td>${estoque.quantidade}</td>
        <td>${formatarEstado(estoque.estado)}</td>
      </tr>
    `;
  });
}

function formatarEstado(estado) {
  if (estado === "DISPONÍVEL") return "🟢 Disponível";
  if (estado === "ALERTA") return "🟡 Alerta";
  if (estado === "ESGOTADO") return "🔴 Esgotado";
  return "-";
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
  const transferencias = await resposta.json();

  const tabela = document.getElementById("tabelaTransferencias");
  tabela.innerHTML = "";

  transferencias.forEach(t => {
    const data = new Date(t.dataTransferencia);

    const dataFormatada = formatarData(data);

    tabela.innerHTML += `
      <tr>
        <td>${t.produto.nome}</td>
        <td>${t.lojaOrigem.nome}</td>
        <td>${t.lojaDestino.nome}</td>
        <td>${t.quantidade}</td>
        <td>${dataFormatada}</td>
      </tr>
    `;
  });
}

async function carregarOrdensCompra() {
  const resposta = await fetch("/ordens-compra");
  const ordens = await resposta.json();
  console.log(ordens);

  const tabela = document.getElementById("tabelaOrdens");
  tabela.innerHTML = "";

  ordens.forEach(o => {
    const data = new Date(o.dataCriacao);
    console.log(data)
    const dataFormatada = formatarData(data);
    console.log(dataFormatada)

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

function carregarTudo() {
  // Dados comuns a todos
  carregarProdutos();
  carregarLojas();
  carregarEstoques();

  // Verifica o tipo de usuário para carregar dados sensíveis
  const tipo = usuarioLogado?.tipoFuncionario || usuarioLogado?.tipo_funcionario;
  console.log(usuarioLogado);

  const areaGestao = document.getElementById("areaGestao");

  if (tipo === "GERENTE" || tipo === "ADMIN") {
  areaGestao.style.display = "block";
  carregarTransferencias();
  carregarOrdensCompra();
  } else {
    areaGestao.style.display = "none";
  }
}

function baixarArquivo(nomeArquivo, conteudo) {

  const blob = new Blob(
    [conteudo],
    { type: "text/plain" }
  );

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

  const tipo =
    usuarioLogado.tipoFuncionario ||
    usuarioLogado.tipo_funcionario;

  if (
    tipo === "GERENTE" &&
    usuarioLogado.loja
  ) {

    transferencias = transferencias.filter(
      t =>
        t.lojaOrigem.id === usuarioLogado.loja.id ||
        t.lojaDestino.id === usuarioLogado.loja.id
    );
  }

  const totalTransferencias =
    transferencias.length;

  const totalItens =
    transferencias.reduce(
      (soma, t) => soma + t.quantidade,
      0
    );

  let texto = `
RELATÓRIO DE TRANSFERÊNCIAS
==================================================

Gerado por: ${usuarioLogado.nome}

Total de Transferências: ${totalTransferencias}
Total de Itens Movimentados: ${totalItens}

==================================================

`;

  transferencias.forEach(t => {

    texto += `
Produto: ${t.produto.nome}
Origem: ${t.lojaOrigem.nome}
Destino: ${t.lojaDestino.nome}
Quantidade: ${t.quantidade}
Data: ${formatarData(t.dataTransferencia)}

--------------------------------------------------

`;
  });

  baixarArquivo(
    "relatorio-transferencias.txt",
    texto
  );
}

async function gerarRelatorioCompras() {

  const resposta =
    await fetch("/ordens-compra");

  let ordens =
    await resposta.json();

  const tipo =
    usuarioLogado.tipoFuncionario ||
    usuarioLogado.tipo_funcionario;

  if (
    tipo === "GERENTE" &&
    usuarioLogado.loja
  ) {

    ordens = ordens.filter(
      o =>
        o.loja.id ===
        usuarioLogado.loja.id
    );
  }

  const totalOrdens =
    ordens.length;

  const totalComprado =
    ordens.reduce(
      (soma, o) => soma + o.quantidade,
      0
    );

  let texto = `
RELATÓRIO DE ORDENS DE COMPRA
==================================================

Gerado por: ${usuarioLogado.nome}

Total de Ordens: ${totalOrdens}
Quantidade Total Comprada: ${totalComprado}

==================================================

`;

  ordens.forEach(o => {

    texto += `
Produto: ${o.produto.nome}
Loja: ${o.loja.nome}
Fornecedor: ${
      o.fornecedor
        ? o.fornecedor.razaoSocial
        : "Sem fornecedor"
    }

Quantidade: ${o.quantidade}
Status: ${o.status}
Data: ${formatarData(o.dataCriacao)}

--------------------------------------------------

`;
  });

  baixarArquivo(
    "relatorio-compras.txt",
    texto
  );
}