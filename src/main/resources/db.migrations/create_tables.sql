CREATE DATABASE BACKEND;
USE BACKEND;

-- ==========================
-- TABELA: USERS
-- ==========================
CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       nome VARCHAR(100) NOT NULL,
                       senha VARCHAR(255) NOT NULL,
                       role ENUM('gerente', 'vendedor') NOT NULL DEFAULT 'vendedor'
);

-- ==========================
-- TABELA: CLIENTES
-- ==========================
CREATE TABLE clientes (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          nome VARCHAR(100) NOT NULL,
                          cpf VARCHAR(20) UNIQUE,
                          telefone VARCHAR(20),
                          criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================
-- TABELA: PRODUTOS
-- ==========================
CREATE TABLE produtos (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          nome VARCHAR(100) NOT NULL,
                          categoria VARCHAR(50),
                          preco DECIMAL(10,2) NOT NULL,
                          quantidade_estoque INT NOT NULL DEFAULT 0,
                          criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================
-- TABELA: VENDAS
-- ==========================
CREATE TABLE vendas (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        cliente_id INT NOT NULL,
                        vendedor_id INT NOT NULL,
                        data_venda TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        valor_total DECIMAL(10,2) DEFAULT 0,
                        FOREIGN KEY (cliente_id) REFERENCES clientes(id),
                        FOREIGN KEY (vendedor_id) REFERENCES users(id)
);

-- ==========================
-- TABELA: VENDA_PRODUTO (N:N)
-- ==========================
CREATE TABLE venda_produto (
                               venda_id INT NOT NULL,
                               produto_id INT NOT NULL,
                               quantidade INT NOT NULL DEFAULT 1,
                               preco_unitario DECIMAL(10,2) NOT NULL,
                               PRIMARY KEY (venda_id, produto_id),
                               FOREIGN KEY (venda_id) REFERENCES vendas(id) ON DELETE CASCADE,
                               FOREIGN KEY (produto_id) REFERENCES produtos(id)
);

ALTER TABLE users
    MODIFY COLUMN role VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL;
