PRAGMA foreign_keys = OFF;

DELETE FROM pedido_itens;
DELETE FROM pedidos;
DELETE FROM ordens_de_servico;
DELETE FROM vendas;
DELETE FROM contas_a_receber;
DELETE FROM contas_pagar;
DELETE FROM compras;
DELETE FROM produtos;
DELETE FROM empresas;
DELETE FROM tecnicos;
DELETE FROM clientes;
DELETE FROM usuarios;
DELETE FROM fornecedores;
DELETE FROM categorias;
DELETE FROM licensas;
DELETE FROM preferencias;

PRAGMA foreign_keys = ON;
