package com.example.domain.core;

public interface CoreUseCase<P, R> {
    R execute(P params);
}