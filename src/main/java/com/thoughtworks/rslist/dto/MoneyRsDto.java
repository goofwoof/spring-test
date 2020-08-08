package com.thoughtworks.rslist.dto;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
@Table(name = "moneyEvent")
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
public class MoneyRsDto {
    @Id
    @GeneratedValue
    private int id;
    private int rank;
    private int money;
    @OneToOne
    @JoinColumn(name = "rs_id")
    private RsEventDto rsEventDto;
}
