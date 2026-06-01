package com.ledger.controller;

import com.ledger.dto.DepositRequest;
import com.ledger.dto.RefundRequest;
import com.ledger.dto.TransferRequest;
import com.ledger.dto.TransactionResponse;
import com.ledger.service.TransactionOrchestrator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionOrchestrator orchestrator;

    public TransactionController(TransactionOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest req) {
        var tx = orchestrator.transfer(
                req.idempotencyKey(), req.sourceWalletId(), req.destinationWalletId(),
                req.amount(), req.fee(), req.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.from(tx));
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody DepositRequest req) {
        var tx = orchestrator.deposit(req.idempotencyKey(), req.walletId(), req.amount(), req.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.from(tx));
    }

    @PostMapping("/refund")
    public ResponseEntity<TransactionResponse> refund(@Valid @RequestBody RefundRequest req) {
        var tx = orchestrator.refund(req.idempotencyKey(), req.originalTransactionRef(), req.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.from(tx));
    }

    @GetMapping("/{transactionRef}")
    public ResponseEntity<TransactionResponse> getByRef(@PathVariable String transactionRef) {
        var tx = orchestrator.getByTransactionRef(transactionRef);
        return ResponseEntity.ok(TransactionResponse.from(tx));
    }

    @GetMapping("/idempotency/{key}")
    public ResponseEntity<TransactionResponse> getByIdempotency(@PathVariable String key) {
        var tx = orchestrator.getByIdempotencyKey(key);
        return ResponseEntity.ok(TransactionResponse.from(tx));
    }
}
