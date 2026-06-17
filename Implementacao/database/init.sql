-- create database projetoaps;
-- DROP DATABASE IF EXISTS projetoaps; 
use projetoaps;

INSERT INTO lojas (id, nome) VALUES
(1, 'MÉIER'),
(2, 'TIJUCA');

INSERT INTO fornecedores (id, razao_social, cnpj, telefone, email, endereco) VALUES
(1, 'DISTRIBUIDORA ABC', '12345678000199', '21999999999', 'contato@abc.com', 'RIO DE JANEIRO'),
(2, 'ALIMENTOS BRASIL', '98765432000155', '21888888888', 'vendas@alimentos.com', 'RIO DE JANEIRO');

INSERT INTO produtos (id, codigo, nome, categoria, valor_compra, valor_venda, qtd_minima, fornecedor_id) VALUES
(1, 'AR001', 'ARROZ 5KG', 'ALIMENTOS', 20.00, 25.90, 10, 1),
(2, 'FE001', 'FEIJÃO 1KG', 'ALIMENTOS', 7.00, 9.90, 10, 1),
(3, 'AC001', 'AÇÚCAR 1KG', 'ALIMENTOS', 4.50, 5.99, 15, 1),
(4, 'CA001', 'CAFÉ 500G', 'ALIMENTOS', 11.00, 14.90, 10, 2),
(5, 'LE001', 'LEITE INTEGRAL', 'BEBIDAS', 4.20, 5.49, 20, 2),
(6, 'RE001', 'REFRIGERANTE 2L', 'BEBIDAS', 6.50, 8.99, 15, 2),
(7, 'DE001', 'DETERGENTE', 'LIMPEZA', 2.10, 2.99, 20, 1),
(8, 'SA001', 'SABÃO EM PÓ', 'LIMPEZA', 9.00, 12.50, 10, 1),
(9, 'PA001', 'PAPEL HIGIÊNICO', 'HIGIENE', 13.00, 17.90, 10, 2),
(10, 'SH001', 'SHAMPOO', 'HIGIENE', 15.00, 19.90, 8, 2);

INSERT INTO funcionarios (nome, email, tipo_funcionario, loja_id) VALUES 
('ANA', 'ana@cariocada.com', 'GERENTE', 1),
('BIA', 'bia@cariocada.com', 'GERENTE', 2),
('MALU', 'malu@cariocada.com', 'ATENDENTE', 1),
('IGOR', 'igor@cariocada.com', 'ATENDENTE', 2),
('ADMINISTRADOR', 'admin@cariocada.com', 'ADMIN', NULL);

INSERT INTO estoques (produto_id, loja_id, quantidade) VALUES
(1, 1, 11),
(2, 1, 5),
(3, 1, 25),
(4, 1, 15),
(5, 1, 8),
(6, 1, 20),
(7, 1, 30),
(8, 1, 7),
(1, 2, 40),
(2, 2, 18),
(3, 2, 12),
(4, 2, 35),
(5, 2, 4),
(6, 2, 28),
(9, 2, 14),
(10, 2, 9);