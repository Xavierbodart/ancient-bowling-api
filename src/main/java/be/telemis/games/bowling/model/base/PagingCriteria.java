package be.telemis.games.bowling.model.base;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PagingCriteria {
    @Min(0)
    private int page;
    @Min(1)
    @Max(500)
    @NotNull
    private int pageSize;
}
