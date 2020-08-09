package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.RsEvent;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.MoneyRsDto;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.MoneyRepository;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
@AutoConfigureMockMvc
class RsServiceTest {
  RsService rsService;
  @Autowired
  private MockMvc mockMvc;
  @Mock RsEventRepository rsEventRepository;
  @Mock UserRepository userRepository;
  @Mock VoteRepository voteRepository;
  @Mock MoneyRepository moneyRepository;
  LocalDateTime localDateTime;
  Vote vote;
  UserDto userDto;
  RsEventDto rsEventDto;

  @BeforeEach
  void setUp() {
    userDto =
            UserDto.builder().voteNum(5).phone("18888888888")
                    .gender("female").email("a@b.com").age(19)
                    .userName("xiaoli").id(2).build();
    rsEventDto=
            RsEventDto.builder().eventName("event name").id(1)
                    .keyword("keyword").voteNum(2).userDto(userDto).build();

    initMocks(this);
    rsService = new RsService(rsEventRepository, userRepository, voteRepository, moneyRepository);
    localDateTime = LocalDateTime.now();
    vote = Vote.builder().voteNum(2).rsEventId(1).time(localDateTime).userId(1).build();
  }

  @Test
  void shouldVoteSuccess() {
    // given
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(userDto));
    // when
    rsService.vote(vote, 1);
    // then
    verify(voteRepository)
        .save(
            VoteDto.builder()
                .num(2)
                .localDateTime(localDateTime)
                .userDto(userDto)
                .rsEventDto(rsEventDto)
                .build());
    verify(userRepository).save(userDto);
    verify(rsEventRepository).save(rsEventDto);
  }

  @Test
  void shouldThrowExceptionWhenUserNotExist() {
    // given
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());
    when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
    //when&then
    assertThrows(
        RuntimeException.class,
        () -> {
          rsService.vote(vote, 1);
        });
  }
  @Test
  void shouldBuySeccess(){
    //given
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
    when(moneyRepository.findByRsEventDtoId(anyInt())).thenReturn(null);
    when(moneyRepository.save(any())).thenThrow(new RuntimeException("Program comes here"));
    Trade trade = Trade.builder().money(50).rank(1).build();
    //when&then
    assertThrows(
            RuntimeException.class,
            () -> {
              rsService.buy(trade, anyInt());
            }).getMessage().equals("Program comes here");
  }

  @Test
  void shouldBuyFailWithLowPrice(){
    //given
    MoneyRsDto moneyRsDto = MoneyRsDto.builder().rank(1).money(50).rsEventDto(rsEventDto).id(0).build();
    rsEventDto.setMoneyRsDto(moneyRsDto);
    RsEventDto rsEventDto1 = RsEventDto.builder().eventName("event name2").id(2)
                    .keyword("keyword").voteNum(2).userDto(userDto).build();

    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto1));

    when(moneyRepository.findByRsEventDtoId(anyInt())).thenReturn(null);
    when(moneyRepository.findByRank(anyInt())).thenReturn(moneyRsDto);
    when(moneyRepository.save(any())).thenThrow(new RuntimeException("Program comes here"));
    Trade trade = Trade.builder().money(10).rank(1).build();
    //when&then
    assertThrows(
            RuntimeException.class,
            () -> {
              rsService.buy(trade, anyInt());
            }).getMessage().equals("low price");
  }

  @Test
  void shouldBuyFailWithRsHasBeenBought(){
    //given
    MoneyRsDto moneyRsDto = MoneyRsDto.builder().rank(1).money(50).rsEventDto(rsEventDto).id(0).build();
    rsEventDto.setMoneyRsDto(moneyRsDto);
    RsEventDto rsEventDto1 = RsEventDto.builder().eventName("event name2").id(2)
            .keyword("keyword").voteNum(2).userDto(userDto).build();

    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));

    when(moneyRepository.findByRsEventDtoId(anyInt())).thenReturn(null);
    when(moneyRepository.findByRank(anyInt())).thenReturn(moneyRsDto);
    when(moneyRepository.save(any())).thenThrow(new RuntimeException("Program comes here"));
    Trade trade = Trade.builder().money(10).rank(1).build();
    //when&then
    assertThrows(
            RuntimeException.class,
            () -> {
              rsService.buy(trade, anyInt());
            }).getMessage().equals("rs has been bought");
  }

  @Test
  void shouldBuyFailWithRsNotExist(){
    //given
    MoneyRsDto moneyRsDto = MoneyRsDto.builder().rank(1).money(50).rsEventDto(rsEventDto).id(0).build();
    rsEventDto.setMoneyRsDto(moneyRsDto);
    RsEventDto rsEventDto1 = RsEventDto.builder().eventName("event name2").id(2)
            .keyword("keyword").voteNum(2).userDto(userDto).build();

    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());

    when(moneyRepository.findByRsEventDtoId(anyInt())).thenReturn(null);
    when(moneyRepository.findByRank(anyInt())).thenReturn(moneyRsDto);
    when(moneyRepository.save(any())).thenThrow(new RuntimeException("Program comes here"));
    Trade trade = Trade.builder().money(10).rank(1).build();
    //when&then
    assertThrows(
            RuntimeException.class,
            () -> {
              rsService.buy(trade, anyInt());
            }).getMessage().equals("rs not exist");
  }



}
