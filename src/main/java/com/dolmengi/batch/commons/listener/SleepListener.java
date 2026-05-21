package com.dolmengi.batch.commons.listener;

import java.time.Duration;
import java.util.Objects;

import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.infrastructure.item.Chunk;

public class SleepListener implements ChunkListener<Object, Object> {

	private final long sleepMillis;

	public SleepListener() {
		this(Duration.ofSeconds(2));
	}

	public SleepListener(Duration sleep) {
		Objects.requireNonNull(sleep, "sleep");
		if (sleep.isNegative()) {
			throw new IllegalArgumentException("sleep must not be negative");
		}

		this.sleepMillis = sleep.toMillis();
	}

	@Override
	public void afterChunk(@NonNull Chunk<Object> chunk) {
		if (sleepMillis == 0L) {
			return;
		}

		try {
			Thread.sleep(sleepMillis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Chunk sleep interrupted", e);
		}
	}

}
