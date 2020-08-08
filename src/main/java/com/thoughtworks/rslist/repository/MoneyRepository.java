package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.dto.MoneyRsDto;
import com.thoughtworks.rslist.dto.RsEventDto;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MoneyRepository extends CrudRepository<MoneyRsDto, Integer> {

    List<MoneyRsDto> findByRsEventDtoId(int rsEventDtoId);

    MoneyRsDto findByRank(int rank);
}
