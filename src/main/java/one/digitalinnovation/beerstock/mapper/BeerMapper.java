package one.digitalinnovation.beerstock.mapper;

import one.digitalinnovation.beerstock.builder.BeerDTO;
import one.digitalinnovation.beerstock.entity.Beer;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BeerMapper {

    BeerMapper INSTANCE = Mappers.getMapper(BeerMapper.class);

    Beer toModel(BeerDTO beerDTO);

    BeerDTO toDTO(Beer beer);

}
