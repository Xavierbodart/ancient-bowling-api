package be.telemis.games.bowling.model.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Sort;


@ParameterObject
@Data
public class SortingCriteria<T extends Enum<T>> {

    @Schema(description = "Field to sort by")
    private T sortField;

    @Schema(description = "Direction of sorting")
    private Sort.Direction sortDirection;
}
