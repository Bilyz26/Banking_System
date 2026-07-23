package com.bankingsystem.customer.application.port.in;

import com.bankingsystem.customer.domain.CustomerId;

public record GetCustomerQuery(CustomerId customerId) {
}

