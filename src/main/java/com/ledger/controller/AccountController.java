package com.ledger.controller;

import com.ledger.domain.Account;
import com.ledger.domain.enums.Currency;
import com.ledger.dto.CreateAccountRequest;
import com.ledger.dto.CreateAccountResponse;
import com.ledger.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<CreateAccountResponse> create(@Valid @RequestBody CreateAccountRequest req) {
        Account account = accountService.createAccount(req.name(), req.currency());
        var wallets = accountService.getWallets(account.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(CreateAccountResponse.from(account, wallets));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> get(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccount(id));
    }

    @PostMapping("/{id}/freeze")
    public ResponseEntity<Void> freeze(@PathVariable Long id) {
        accountService.freezeAccount(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/unfreeze")
    public ResponseEntity<Void> unfreeze(@PathVariable Long id) {
        accountService.unfreezeAccount(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<Long> getBalance(@PathVariable Long id, @RequestParam Currency currency) {
        return ResponseEntity.ok(accountService.getBalance(id, currency));
    }
}
