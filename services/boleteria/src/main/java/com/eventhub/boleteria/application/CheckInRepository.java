package com.eventhub.boleteria.application;

import com.eventhub.boleteria.domain.CheckIn;

/** Guarda quién validó cada ingreso y cuándo (RF-03). */
public interface CheckInRepository {
    void registrar(CheckIn checkIn);
}
