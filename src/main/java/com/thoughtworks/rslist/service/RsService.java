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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class RsService {
  final RsEventRepository rsEventRepository;
  final UserRepository userRepository;
  final VoteRepository voteRepository;
  final MoneyRepository moneyRepository;


  public RsService(RsEventRepository rsEventRepository, UserRepository userRepository, VoteRepository voteRepository, MoneyRepository moneyRepository) {
    this.rsEventRepository = rsEventRepository;
    this.userRepository = userRepository;
    this.voteRepository = voteRepository;
    this.moneyRepository = moneyRepository;
  }

  public void vote(Vote vote, int rsEventId) {
    Optional<RsEventDto> rsEventDto = rsEventRepository.findById(rsEventId);
    Optional<UserDto> userDto = userRepository.findById(vote.getUserId());
    if (!rsEventDto.isPresent()
        || !userDto.isPresent()
        || vote.getVoteNum() > userDto.get().getVoteNum()) {
      throw new RuntimeException();
    }
    VoteDto voteDto =
        VoteDto.builder()
            .localDateTime(vote.getTime())
            .num(vote.getVoteNum())
            .rsEventDto(rsEventDto.get())
            .userDto(userDto.get())
            .build();
    voteRepository.save(voteDto);
    UserDto user = userDto.get();
    user.setVoteNum(user.getVoteNum() - vote.getVoteNum());
    userRepository.save(user);
    RsEventDto rsEvent = rsEventDto.get();
    rsEvent.setVoteNum(rsEvent.getVoteNum() + vote.getVoteNum());
    rsEventRepository.save(rsEvent);
  }

  public void buy(Trade trade, int id){
    if(!rsEventRepository.findById(id).isPresent()){
      throw new RuntimeException("rs not exist");
    }
    RsEventDto rsEventDto = rsEventRepository.findById(id).get();
    //if(!moneyRepository.findByRsEventDtoId(rsEventDto.getId()).isEmpty()){
    if(rsEventDto.getMoneyRsDto() != null){
      throw new RuntimeException("rs has been bought");
    }
    MoneyRsDto moneyRsDto = moneyRepository.findByRank(trade.getRank());
    if(moneyRsDto != null && moneyRsDto.getMoney() >= trade.getMoney()){
      throw new RuntimeException("low price");
    }
    if (moneyRsDto == null) {
      createBuy(trade, rsEventDto);
    } else {
      updateBuy(moneyRsDto, trade, rsEventDto);
    }
  }

  private void updateBuy(MoneyRsDto moneyRsDto, Trade trade, RsEventDto rsEventDto) {
    moneyRsDto.setMoney(trade.getMoney());
    moneyRsDto.setRank(trade.getRank());
    moneyRsDto.setRsEventDto(rsEventDto);
    moneyRepository.save(moneyRsDto);
  }


  private void createBuy(Trade trade, RsEventDto rsEventDto) {
    MoneyRsDto moneyRsDto = MoneyRsDto.builder().rsEventDto(rsEventDto).money(trade.getMoney())
            .rank(trade.getRank()).build();
    moneyRepository.save(moneyRsDto);
  }

  public List<?> getOrderedRsevents() {
    List<RsEventDto> rsEventDtoList = rsEventRepository.findAll();
    rsEventDtoList.stream().forEach(rsEventDto -> rsEventDto.setVoteNum(
            rsEventDto.getVoteDtoList().stream().mapToInt(VoteDto::getNum).sum()));
    rsEventDtoList = rsEventDtoList.stream().sorted(Comparator.comparing(RsEventDto::getVoteNum).reversed()).collect(Collectors.toList());

    List<RsEventDto> moneyList = new ArrayList<>();
    rsEventDtoList = rsEventDtoList.stream().filter(rsEventDto ->
          { if(rsEventDto.getMoneyRsDto()!= null){
            moneyList.add(rsEventDto);
            return false;
          }
          else{
            return true;
          }}).collect(Collectors.toList());
    List<RsEventDto> finalRsEventDtoList = new ArrayList<>(rsEventDtoList);
    moneyList.forEach(rsEventDto -> finalRsEventDtoList.add(Math.min(rsEventDto.getMoneyRsDto().getRank() -1, finalRsEventDtoList.size()), rsEventDto));
    List<RsEvent> list = finalRsEventDtoList.stream()
            .map(item -> RsEvent.builder()
                    .eventName(item.getEventName())
                    .keyword(item.getKeyword())
                    .userId(item.getId())
                    .voteNum(item.getVoteNum())
                    .build())
            .collect(Collectors.toList());
    AtomicInteger index = new AtomicInteger(1);
    list.stream().forEach(rsEvent -> rsEvent.setRsOrder(index.getAndIncrement()));
    return list;
  }

}
