USE projetoaps;

-- FORNECEDORES

INSERT INTO fornecedores (
    razao_social,
    cnpj,
    telefone,
    email,
    endereco
)
VALUES (
    'Distribuidora ABC',
    '12345678000199',
    '21999999999',
    'contato@abc.com',
    'Rio de Janeiro'
);

-- LOJAS

INSERT INTO lojas (nome)
VALUES ('Méier');

INSERT INTO lojas (nome)
VALUES ('Tijuca');

-- PRODUTOS

INSERT INTO produtos (
    codigo,
    nome,
    categoria,
    valor_venda,
    qtd_minima,
    fornecedor_id
)
VALUES (
    'AR001',
    'Arroz',
    'Alimentos',
    25.90,
    10,
    1
);

INSERT INTO produtos (
    codigo,
    nome,
    categoria,
    valor_venda,
    qtd_minima,
    fornecedor_id
)
VALUES (
    'FE001',
    'Feijão',
    'Alimentos',
    9.90,
    10,
    1
);

-- ESTOQUES

INSERT INTO estoques (
    produto_id,
    loja_id,
    quantidade
)
VALUES (
    1,
    1,
    11
);

INSERT INTO estoques (
    produto_id,
    loja_id,
    quantidade
)
VALUES (
    1,
    2,
    40
);

INSERT INTO estoques (
    produto_id,
    loja_id,
    quantidade
)
VALUES (
    2,
    1,
    20
);

INSERT INTO estoques (
    produto_id,
    loja_id,
    quantidade
)
VALUES (
    2,
    2,
    20
);