CREATE TABLE accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
);

CREATE TABLE wallets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    currency VARCHAR(10) NOT NULL,
    balance BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_wallets_account FOREIGN KEY (account_id) REFERENCES accounts(id)
);

CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_ref VARCHAR(30) NOT NULL UNIQUE,
    idempotency_key VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    source_wallet_id BIGINT NOT NULL,
    destination_wallet_id BIGINT,
    amount BIGINT NOT NULL,
    fee BIGINT NOT NULL DEFAULT 0,
    description VARCHAR(500),
    failure_reason VARCHAR(500),
    created_at DATETIME(6) NOT NULL,
    completed_at DATETIME(6),
    CONSTRAINT uq_idempotency_key UNIQUE (idempotency_key)
);

CREATE TABLE ledger_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_ref VARCHAR(30) NOT NULL,
    wallet_id BIGINT NOT NULL,
    entry_type VARCHAR(10) NOT NULL,
    amount BIGINT NOT NULL,
    running_balance BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    INDEX idx_ledger_wallet (wallet_id, created_at DESC),
    INDEX idx_ledger_txn (transaction_ref)
);

CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_ref VARCHAR(30) NOT NULL,
    from_status VARCHAR(20) NOT NULL,
    to_status VARCHAR(20) NOT NULL,
    notes VARCHAR(500),
    created_at DATETIME(6) NOT NULL,
    INDEX idx_audit_txn (transaction_ref, created_at ASC)
);
