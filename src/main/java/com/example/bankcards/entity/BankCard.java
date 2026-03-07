package com.example.bankcards.entity;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;
import com.example.bankcards.entity.enums.CardStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@ToString
@Table(name = "bankcard")
public class BankCard {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(unique = true)
    @NotNull  
    private Long cardNumber;

    @JoinColumn(name = "customer_id")
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = "Customer is required")
    private Customer customer;

    @Column(name = "expire_date", nullable = false)
    @NotNull(message = "Expiration date is required")
    @Future(message = "Expiration date must be in the future")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expireDate;

    @Column(name = "status" , nullable = false)
    @Enumerated(EnumType.STRING)
    private CardStatus status; 

    @Column
    @NotNull
    private BigDecimal balance = BigDecimal.ZERO;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    public String getNumber() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getNumber'");
    }
}
