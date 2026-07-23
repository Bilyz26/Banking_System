package com.bankingsystem.transfer.application.port.in;

@FunctionalInterface
public interface TransferMoneyUseCase {

    TransferMoneyResult transfer(TransferMoneyCommand command);
}
