package com.wex.purchaser.fiscalapi;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse  implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@JsonProperty("data")
	private List<ExchangeRate> data;
}
