package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.dto.RsEventDto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface RsEventRepository extends CrudRepository<RsEventDto, Integer> {
    List<RsEventDto> findAll();

    @Transactional
    void deleteAllByUserDtoId(int userDtoId);

    //List<RsEventDto> findOrderByVoteNum();
}
