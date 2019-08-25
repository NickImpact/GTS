package me.nickimpact.gts.api.deprecated;

import com.nickimpact.impactor.api.json.JsonTyping;

import java.math.BigDecimal;

@JsonTyping("money")
@Deprecated
public class MoneyPrice extends Price<BigDecimal> {}
