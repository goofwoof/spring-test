package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.dto.MoneyRsDto;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.MoneyRepository;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VoteControllerTest {
  @Autowired MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RsEventRepository rsEventRepository;
    @Autowired
    VoteRepository voteRepository;
    @Autowired
    MoneyRepository moneyRepository;
    UserDto userDto;
    RsEventDto rsEventDto;

    @BeforeEach
    void setUp() {

        moneyRepository.deleteAll();
        voteRepository.deleteAll();
        rsEventRepository.deleteAll();
        userRepository.deleteAll();

        userDto = UserDto.builder().userName("idolice").age(19).email("a@b.com").gender("female")
                .phone("18888888888").voteNum(10).build();
        userDto = userRepository.save(userDto);
        rsEventDto = RsEventDto.builder().userDto(userDto).eventName("event name").keyword("keyword").voteNum(0)
                    .build();
        rsEventDto = rsEventRepository.save(rsEventDto);
        VoteDto voteDto = VoteDto.builder().userDto(userDto).rsEventDto(rsEventDto).localDateTime(LocalDateTime.now())
                .num(5).build();
        voteRepository.save(voteDto);
      }

    @Test
    public void shouldGetVoteRecord() throws Exception {
      VoteDto voteDto = VoteDto.builder().userDto(userDto).rsEventDto(rsEventDto).localDateTime(LocalDateTime.now())
              .num(1).build();
      voteRepository.save(voteDto);
      voteDto = VoteDto.builder().userDto(userDto).rsEventDto(rsEventDto).localDateTime(LocalDateTime.now())
              .num(2).build();
      voteRepository.save(voteDto);

      voteDto = VoteDto.builder().userDto(userDto).rsEventDto(rsEventDto).localDateTime(LocalDateTime.now())
              .num(3).build();
      voteRepository.save(voteDto);

      voteDto = VoteDto.builder().userDto(userDto).rsEventDto(rsEventDto).localDateTime(LocalDateTime.now())
              .num(4).build();
      voteRepository.save(voteDto);

      voteDto = VoteDto.builder().userDto(userDto).rsEventDto(rsEventDto).localDateTime(LocalDateTime.now())
              .num(6).build();
      voteRepository.save(voteDto);

      voteDto = VoteDto.builder().userDto(userDto).rsEventDto(rsEventDto).localDateTime(LocalDateTime.now())
              .num(7).build();
      voteRepository.save(voteDto);

      voteDto = VoteDto.builder().userDto(userDto).rsEventDto(rsEventDto).localDateTime(LocalDateTime.now())
              .num(8).build();
      voteRepository.save(voteDto);



      mockMvc.perform(get("/voteRecord").param("userId",String.valueOf(userDto.getId()))
      .param("rsEventId",String.valueOf(rsEventDto.getId())).param("pageIndex", "1"))
              .andExpect(jsonPath("$", hasSize(5)))
              .andExpect(jsonPath("$[0].userId",is(userDto.getId())))
              .andExpect(jsonPath("$[0].rsEventId",is(rsEventDto.getId())))
              .andExpect(jsonPath("$[0].voteNum",is(5)))
              .andExpect(jsonPath("$[1].voteNum",is(1)))
              .andExpect(jsonPath("$[2].voteNum",is(2)))
              .andExpect(jsonPath("$[3].voteNum",is(3)))
              .andExpect(jsonPath("$[4].voteNum",is(4)));

      mockMvc.perform(get("/voteRecord").param("userId",String.valueOf(userDto.getId()))
              .param("rsEventId",String.valueOf(rsEventDto.getId())).param("pageIndex", "2"))
              .andExpect(jsonPath("$", hasSize(3)))
              .andExpect(jsonPath("$[0].userId",is(userDto.getId())))
              .andExpect(jsonPath("$[0].rsEventId",is(rsEventDto.getId())))
              .andExpect(jsonPath("$[0].voteNum",is(6)))
              .andExpect(jsonPath("$[1].voteNum",is(7)))
              .andExpect(jsonPath("$[2].voteNum",is(8)));


    }


    @Test
    public void should_return_rs_ordered_when_get_rsEventDto() throws Exception {

        UserDto save = userRepository.save(userDto);

        RsEventDto rsEventDto1 =
                RsEventDto.builder().keyword("no").eventName("e1").userDto(save).build();
        rsEventDto1 = rsEventRepository.save(rsEventDto1);
        RsEventDto rsEventDto2 =
                RsEventDto.builder().keyword("no").eventName("e2").userDto(save).build();
        rsEventDto2 = rsEventRepository.save(rsEventDto2);
        RsEventDto rsEventDto3 =
                RsEventDto.builder().keyword("no").eventName("e3").userDto(save).build();
        rsEventDto3 = rsEventRepository.save(rsEventDto3);

        VoteDto voteDto1 =
                VoteDto.builder().userDto(userDto).rsEventDto(rsEventDto1).localDateTime(LocalDateTime.now())
                .num(3).build();
        voteRepository.save(voteDto1);

        MoneyRsDto moneyRsDto = MoneyRsDto.builder().rsEventDto(rsEventDto2).money(50)
                .rank(1).build();
        moneyRepository.save(moneyRsDto);

        moneyRsDto = MoneyRsDto.builder().rsEventDto(rsEventDto3).money(50)
                .rank(3).build();
        moneyRepository.save(moneyRsDto);

        mockMvc
                .perform(get("/rs/list?start=1&end=4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));

        mockMvc.perform(get("/rs/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].eventName", is(rsEventDto2.getEventName())))
                .andExpect(jsonPath("$[1].eventName", is(rsEventDto.getEventName())))
                .andExpect(jsonPath("$[2].eventName", is(rsEventDto3.getEventName())))
                .andExpect(jsonPath("$[3].eventName", is(rsEventDto1.getEventName())));
    }
}
