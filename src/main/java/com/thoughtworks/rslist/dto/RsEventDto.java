package com.thoughtworks.rslist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rsEvent")
public class RsEventDto {
  @Id @GeneratedValue private int id;
  private String eventName;
  private String keyword;
  private int voteNum;
  @ManyToOne
  @JoinColumn(name = "user_id")
  private UserDto userDto;

  @OneToOne(mappedBy =  "rsEventDto")
  private MoneyRsDto moneyRsDto;

  @OneToMany(mappedBy = "rsEventDto")
  private List<VoteDto> voteDtoList;
}
