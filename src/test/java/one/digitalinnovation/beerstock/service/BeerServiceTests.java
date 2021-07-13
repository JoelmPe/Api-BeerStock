package one.digitalinnovation.beerstock.service;

import one.digitalinnovation.beerstock.builder.BeerDTO;
import one.digitalinnovation.beerstock.entity.Beer;
import one.digitalinnovation.beerstock.exception.BeerAlreadyRegisteredException;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.exception.BeerStockExceededException;
import one.digitalinnovation.beerstock.mapper.BeerMapper;
import one.digitalinnovation.beerstock.repository.BeerRepository;
import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BeerServiceTests {

    private static final long INVALID_BEER_ID= 1L;

    @Mock
    private BeerRepository beerRepository;

    private BeerMapper beerMapper = BeerMapper.INSTANCE;

    @InjectMocks
    private BeerService beerService;

    @Test
    void whenBeerInformedThenItShouldBeCreated() throws BeerAlreadyRegisteredException {
        //Given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedSavedBeer = beerMapper.toModel(expectedBeerDTO);

        //When
        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.empty());
        when(beerRepository.save(expectedSavedBeer)).thenReturn(expectedSavedBeer);

        //Then
        BeerDTO createdBeerDTO= beerService.createBeer(expectedBeerDTO);

        assertThat(createdBeerDTO.getId(), is(equalTo(expectedBeerDTO.getId())));
        assertThat(createdBeerDTO.getName(), is(equalTo(expectedBeerDTO.getName())));
        assertThat(createdBeerDTO.getQuantity(), is(equalTo(expectedBeerDTO.getQuantity())));

        assertThat(createdBeerDTO.getQuantity(), is(greaterThan(2)));
    }

    @Test
    void whenAlreadyRegisteredBeerInformedThenExceptionShouldBeThrown() throws BeerAlreadyRegisteredException {
        //Given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer duplicateBeer = beerMapper.toModel(expectedBeerDTO);

        //When
        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.of(duplicateBeer));

        //Then
        assertThrows(BeerAlreadyRegisteredException.class, () -> beerService.createBeer(expectedBeerDTO));
    }

    @Test
    void whenValidBeerNameIsGivenThenReturnABeer() throws BeerNotFoundException {
        //Given
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);

        //When
        when(beerRepository.findByName(expectedFoundBeer.getName())).thenReturn(Optional.of(expectedFoundBeer));

        //Then
        BeerDTO foundBeerDTO = beerService.findByName(expectedFoundBeerDTO.getName());

        assertThat(foundBeerDTO, is(equalTo(expectedFoundBeerDTO)));

    }

    @Test
    void whenNoRegisteredBeerNameIsGivenThenThrowAnException() {
        //Given
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        //When
        when(beerRepository.findByName(expectedFoundBeerDTO.getName())).thenReturn(Optional.empty());

        //Then
        assertThrows(BeerNotFoundException.class, () -> beerService.findByName(expectedFoundBeerDTO.getName()));
    }

    @Test
    void whenListBeerIsCalledThenReturnAListOfBeers() {
        //Given
        BeerDTO expectedFoundBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBeer = beerMapper.toModel(expectedFoundBeerDTO);

        //When
        when(beerRepository.findAll()).thenReturn(Collections.singletonList(expectedFoundBeer));

        //Then
        List<BeerDTO> foundListBeerDTO = beerService.listAll();

        assertThat(foundListBeerDTO, is(not(empty())));
        assertThat(foundListBeerDTO.get(0), is(equalTo(expectedFoundBeerDTO)));


    }

    @Test
    void whenListBeerIsCalledThenReturnAnEmptyListOfBeers() {
        //When
        when(beerRepository.findAll()).thenReturn(Collections.EMPTY_LIST);

        //Then
        List<BeerDTO> foundListBeerDTO = beerService.listAll();

        assertThat(foundListBeerDTO, is(empty()));


    }

    @Test
    void whenExclusionIsCalledWithValidIdThenBeerShouldBeDeleted() throws BeerNotFoundException {
        //Given
        BeerDTO expectedDeleteBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedDeleteBeer = beerMapper.toModel(expectedDeleteBeerDTO);

        //When
        when(beerRepository.findById(expectedDeleteBeerDTO.getId())).thenReturn(Optional.of(expectedDeleteBeer));
        doNothing().when(beerRepository).deleteById(expectedDeleteBeerDTO.getId());

        //Then
        beerService.deleteById(expectedDeleteBeerDTO.getId());

        verify(beerRepository, times(1)).findById(expectedDeleteBeerDTO.getId());
        verify(beerRepository, times(1)).deleteById(expectedDeleteBeerDTO.getId());

    }

    @Test
    void whenIncrementIsCalledThenIncrementBeerStock() throws BeerNotFoundException, BeerStockExceededException {
        //Given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //When
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));
        when(beerRepository.save(expectedBeer)).thenReturn(expectedBeer);

        int quantityToIncrement = 10;
        int expectedQuantityAfterIncrement = expectedBeerDTO.getQuantity() + quantityToIncrement;

        //Then
        BeerDTO incrementedBeerDTO = beerService.increment(expectedBeerDTO.getId(), quantityToIncrement);

        assertThat(expectedQuantityAfterIncrement, equalTo(incrementedBeerDTO.getQuantity()));
        assertThat(expectedQuantityAfterIncrement, lessThan(expectedBeerDTO.getMax()));
    }

    @Test
    void whenIncrementIsGreatherThanMaxThenThrowException() {
        //Given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //When
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));

        int quantityToIncrement = 80;
        assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
    }

    @Test
    void whenIncrementAfterSumIsGreatherThanMaxThenThrowException() {
        //Given
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedBeer = beerMapper.toModel(expectedBeerDTO);

        //When
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(expectedBeer));

        int quantityToIncrement = 45;
        assertThrows(BeerStockExceededException.class, () -> beerService.increment(expectedBeerDTO.getId(), quantityToIncrement));
    }

    @Test
    void whenIncrementIsCalledWithInvalidIdThenThrowException() {
        //Given
        int quantityToIncrement = 10;

        //When
        when(beerRepository.findById(INVALID_BEER_ID)).thenReturn(Optional.empty());

        assertThrows(BeerNotFoundException.class, () -> beerService.increment(INVALID_BEER_ID, quantityToIncrement));
    }
}
