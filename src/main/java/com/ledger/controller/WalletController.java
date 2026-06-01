package com.ledger.controller;

import com.ledger.domain.LedgerEntry;
import com.ledger.service.LedgerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final LedgerService ledgerService;

    public WalletController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/{id}/statement")
    public ResponseEntity<List<LedgerEntry>> getStatement(@PathVariable Long id) {
        return ResponseEntity.ok(ledgerService.getStatement(id));
    }
}
