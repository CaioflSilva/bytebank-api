package com.bytebank.mapper;

import com.bytebank.dto.ContaRequest;
import com.bytebank.dto.ContaResponse;
import com.bytebank.model.Conta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContaMapper {

    @Mapping(target = "id", ignore = true)
    Conta toEntity(ContaRequest request);

    ContaResponse toResponse(Conta conta);

    List<ContaResponse> toResponseList(List<Conta> contas);
}
