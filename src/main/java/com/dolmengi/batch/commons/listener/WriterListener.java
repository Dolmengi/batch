package com.dolmengi.batch.commons.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.Chunk;

@Slf4j
@RequiredArgsConstructor
public class WriterListener implements ChunkListener<Object, Object> {

	private final StepExecution stepExecution;

	@Override
	public void afterChunk(@NonNull Chunk<Object> chunk) {
		log.info("after write readCount {} commit {}", stepExecution.getReadCount(), stepExecution.getCommitCount());
	}

}
