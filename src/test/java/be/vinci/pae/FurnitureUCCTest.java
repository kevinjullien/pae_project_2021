package be.vinci.pae;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import be.vinci.pae.domain.interfaces.EditionDTO;
import be.vinci.pae.domain.interfaces.FurnitureDTO;
import be.vinci.pae.domain.interfaces.FurnitureDTO.Condition;
import be.vinci.pae.domain.interfaces.OptionDTO;
import be.vinci.pae.domain.interfaces.PhotoDTO;
import be.vinci.pae.domain.interfaces.TypeOfFurnitureDTO;
import be.vinci.pae.domain.interfaces.UserDTO;
import be.vinci.pae.domain.interfaces.UserDTO.Role;
import be.vinci.pae.exceptions.BusinessException;
import be.vinci.pae.exceptions.UnauthorizedException;
import be.vinci.pae.services.dao.interfaces.FurnitureDAO;
import be.vinci.pae.services.dao.interfaces.UserDAO;
import be.vinci.pae.ucc.interfaces.FurnitureUCC;
import be.vinci.pae.utils.ApplicationBinder;
import be.vinci.pae.utils.Config;

public class FurnitureUCCTest {

  private static FurnitureUCC furnitureUCC;
  private static FurnitureDAO furnitureDAO;
  private static FurnitureDTO goodFurniture;
  private static FurnitureDTO badFurniture;
  private static OptionDTO goodOption;
  private static PhotoDTO photo1;
  private static PhotoDTO photo2;
  private static UserDTO goodUser;
  private static String goodReason = "good cancelationReason";
  private static TypeOfFurnitureDTO goodType;
  private static UserDAO userDAO;

  /**
   * Initialisation before every tests.
   */
  @BeforeAll
  public static void init() {
    Config.load();


    ServiceLocator locator =
        ServiceLocatorUtilities.bind(new ApplicationBinder(), new ApplicationBinderTest());
    furnitureUCC = locator.getService(FurnitureUCC.class);

    furnitureDAO = locator.getService(FurnitureDAO.class);

    userDAO = locator.getService(UserDAO.class);
  }

  /**
   * Resetting before each test.
   */
  @BeforeEach
  public void reset() {
    Mockito.reset(furnitureDAO);
    goodFurniture = ObjectDistributor.getFurnitureForFurnitureUCCTest();
    badFurniture = ObjectDistributor.getFurnitureForFurnitureUCCTest();
    goodOption = ObjectDistributor.getGoodOption();
    photo1 = ObjectDistributor.createPhoto();
    photo2 = ObjectDistributor.createPhoto();
    goodUser = ObjectDistributor.getGoodValidatedUser();
    goodType = ObjectDistributor.getGoodTypeOfFurniture();
  }

  @DisplayName("Test getting the option by id with a valid id")
  @Test
  public void getOptionTest1() {
    int id = goodOption.getId();
    Mockito.when(furnitureDAO.getOption(id)).thenReturn(goodOption);
    assertEquals(goodOption, furnitureUCC.getOption(id));
  }

  @DisplayName("Test sum of options days with unvalid furniture id and unvalid user id")
  @Test
  public void getSumOfOptionDaysForAUserAboutAFurnitureTest1() {
    int furnitureId = -5;
    int userId = -5;
    assertEquals(0, furnitureUCC.getSumOfOptionDaysForAUserAboutAFurniture(furnitureId, userId));
  }

  @DisplayName("Test sum of options days with unvalid furniture id and valid user id")
  @Test
  public void getSumOfOptionDaysForAUserAboutAFurnitureTest2() {
    int furnitureId = -5;
    int userId = ObjectDistributor.getGoodValidatedUser().getId();
    assertEquals(0, furnitureUCC.getSumOfOptionDaysForAUserAboutAFurniture(furnitureId, userId));
  }

  @DisplayName("Test sum of options days with unvalid furniture id and valid user id")
  @Test
  public void getSumOfOptionDaysForAUserAboutAFurnitureTest3() {
    int furnitureId = ObjectDistributor.getFurnitureInSale().getId();
    int userId = -5;
    assertEquals(0, furnitureUCC.getSumOfOptionDaysForAUserAboutAFurniture(furnitureId, userId));
  }

  @DisplayName("Test sum of options days with valid furniture id and valid user id")
  @Test
  public void getSumOfOptionDaysForAUserAboutAFurnitureTest4() {
    int furnitureId = ObjectDistributor.getFurnitureInSale().getId();
    int userId = ObjectDistributor.getGoodValidatedUser().getId();
    Mockito.when(furnitureDAO.getSumOfOptionDaysForAUserAboutAFurniture(furnitureId, userId))
        .thenReturn(3);
    assertEquals(3, furnitureUCC.getSumOfOptionDaysForAUserAboutAFurniture(furnitureId, userId));
  }

  @DisplayName("Test to indicate sentToWorkshop with invalid condition furniture")
  @Test
  public void indicateSentToWorkshopTest1() {
    badFurniture.setCondition(Condition.EN_VENTE.toString());
    Mockito.when(furnitureDAO.getFurnitureById(badFurniture.getId())).thenReturn(badFurniture);
    assertThrows(BusinessException.class,
        () -> furnitureUCC.indicateSentToWorkshop(badFurniture.getId()));
  }

  @DisplayName("Test to indicate sentToWorkshop with a valid condition furniture")
  @Test
  public void indicateSentToWorkshopTest2() {
    goodFurniture.setCondition(Condition.ACHETE.toString());
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertDoesNotThrow(() -> furnitureUCC.indicateSentToWorkshop(goodFurniture.getId()));
  }

  @DisplayName("Test to indicate sentToWorkshop with an invalid furniture id")
  @Test
  public void indicateSentToWorkshopTest3() {
    Mockito.when(furnitureDAO.getFurnitureById(-1)).thenReturn(null);
    assertThrows(BusinessException.class,
        () -> furnitureUCC.indicateSentToWorkshop(goodFurniture.getId()));
  }

  @DisplayName("Test to indicate dropOfStore with a invalid condition furniture")
  @Test
  public void indicateDropOfStoreTest1() {
    badFurniture.setCondition(Condition.EN_VENTE.toString());
    Mockito.when(furnitureDAO.getFurnitureById(badFurniture.getId())).thenReturn(badFurniture);
    assertThrows(BusinessException.class,
        () -> furnitureUCC.indicateDropOfStore(badFurniture.getId()));
  }

  @DisplayName("Test to indicate dropOfStore with a valid condition furniture")
  @Test
  public void indicateDropOfStoreTest2() {
    goodFurniture.setCondition(Condition.EN_RESTAURATION.toString());
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertDoesNotThrow(() -> furnitureUCC.indicateDropOfStore(goodFurniture.getId()));
  }

  @DisplayName("Test to indicate dropOfStore with a second valid condition furniture")
  @Test
  public void indicateDropOfStoreTest3() {
    goodFurniture.setCondition(Condition.ACHETE.toString());
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertDoesNotThrow(() -> furnitureUCC.indicateDropOfStore(goodFurniture.getId()));
  }

  @DisplayName("Test to indicate dropOfStore with an invalid furniture id")
  @Test
  public void indicateDropOfStoreTest4() {
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(null);
    assertThrows(BusinessException.class, () -> furnitureUCC.indicateDropOfStore(-1));
  }


  @DisplayName("Test to indicate offerdForSale with a valid condition furniture and invalid price")
  @Test
  public void indicateOfferedForSaleTest1() {
    double price = -8;
    goodFurniture.setCondition(Condition.DEPOSE_EN_MAGASIN.toString());
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertThrows(BusinessException.class,
        () -> furnitureUCC.indicateOfferedForSale(goodFurniture.getId(), price));
  }

  @DisplayName("Test to indicate offerdForSale with a invalid condition furniture and valid price")
  @Test
  public void indicateOfferedForSaleTest2() {
    double price = 22;
    badFurniture.setCondition(Condition.ACHETE.toString());
    Mockito.when(furnitureDAO.getFurnitureById(badFurniture.getId())).thenReturn(badFurniture);
    assertThrows(BusinessException.class,
        () -> furnitureUCC.indicateOfferedForSale(badFurniture.getId(), price));
  }

  @DisplayName("Test to indicate offerdForSale with a valid condition furniture and valid price")
  @Test
  public void indicateOfferedForSaleTest3() {
    double price = 22;
    goodFurniture.setCondition(Condition.DEPOSE_EN_MAGASIN.toString());
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertDoesNotThrow(() -> furnitureUCC.indicateOfferedForSale(goodFurniture.getId(), price));
  }

  @DisplayName("Test to indicate offerdForSale with an invalid furniture id")
  @Test
  public void indicateOfferedForSaleTest4() {
    double price = 22;
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(null);
    assertThrows(BusinessException.class, () -> furnitureUCC.indicateOfferedForSale(-1, price));
  }


  @DisplayName("Test to withdraw a sale with a invalid condition furniture")
  @Test
  public void withdrawSaleTest1() {
    badFurniture.setCondition(Condition.ACHETE.toString());
    Mockito.when(furnitureDAO.getFurnitureById(badFurniture.getId())).thenReturn(badFurniture);
    assertThrows(BusinessException.class, () -> furnitureUCC.withdrawSale(badFurniture.getId()));
  }

  @DisplayName("Test to withdraw a sale with a valid condition furniture")
  @Test
  public void withdrawSaleTest2() {
    goodFurniture.setCondition(Condition.EN_VENTE.toString());
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertDoesNotThrow(() -> furnitureUCC.withdrawSale(goodFurniture.getId()));
  }

  @DisplayName("Test to withdraw a sale with a second valid condition furniture")
  @Test
  public void withdrawSaleTest3() {
    goodFurniture.setCondition(Condition.DEPOSE_EN_MAGASIN.toString());
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertDoesNotThrow(() -> furnitureUCC.withdrawSale(goodFurniture.getId()));
  }

  @DisplayName("Test to withdraw a sale with a second valid condition furniture")
  @Test
  public void withdrawSaleTest4() {
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(null);
    assertThrows(BusinessException.class, () -> furnitureUCC.withdrawSale(-1));
  }

  @DisplayName("Test  to introduce a option with a term <= 0")
  @Test
  public void introduceOptionTest1() {
    int term = 0;
    assertThrows(UnauthorizedException.class,
        () -> furnitureUCC.introduceOption(term, goodUser.getId(), goodFurniture.getId()));
  }

  @DisplayName("Test to introduce a option when the alowed day are already reach")
  @Test
  public void introduceOptionTest2() {
    Mockito.when(furnitureDAO.getSumOfOptionDaysForAUserAboutAFurniture(goodFurniture.getId(),
        goodUser.getId())).thenReturn(5);
    assertThrows(UnauthorizedException.class,
        () -> furnitureUCC.introduceOption(2, goodUser.getId(), goodFurniture.getId()));
  }

  @DisplayName("Test to introduce a option with a sum >5")
  @Test
  public void introduceOptionTest3() {
    int term = 3;
    Mockito.when(furnitureDAO.getSumOfOptionDaysForAUserAboutAFurniture(goodFurniture.getId(),
        goodUser.getId())).thenReturn(3);
    assertThrows(UnauthorizedException.class,
        () -> furnitureUCC.introduceOption(term, goodUser.getId(), goodFurniture.getId()));
  }

  @DisplayName("Test to introduce a good option ")
  @Test
  public void introduceOptionTest4() {
    int term = 2;
    Mockito.when(furnitureDAO.getSumOfOptionDaysForAUserAboutAFurniture(goodFurniture.getId(),
        goodUser.getId())).thenReturn(3);
    assertDoesNotThrow(
        () -> furnitureUCC.introduceOption(term, goodUser.getId(), goodFurniture.getId()));
  }

  @DisplayName("Test to introduce a option with an amount >5")
  @Test
  public void introduceOptionTest5() {
    int term = 6;
    assertThrows(UnauthorizedException.class,
        () -> furnitureUCC.introduceOption(term, goodUser.getId(), goodFurniture.getId()));
  }

  @DisplayName("Test to cancel an option  with a id <= 0")
  @Test
  public void cancelOptionTest1() {
    int id = -5;
    assertThrows(BusinessException.class,
        () -> furnitureUCC.cancelOption(goodReason, id, goodUser));
  }

  @DisplayName("Test to cancel an option with a different id user")
  @Test
  public void cancelOptionTest2() {
    int id = goodUser.getId() + 1;
    goodOption.setIdUser(id);
    goodUser.setRole("client");
    Mockito.when(furnitureDAO.getOption(goodOption.getId())).thenReturn(goodOption);
    assertThrows(BusinessException.class,
        () -> furnitureUCC.cancelOption(goodReason, goodOption.getId(), goodUser));
  }

  @DisplayName("Test to cancel an option with a similar id")
  @Test
  public void cancelOptionTest3() {
    int id = goodUser.getId();
    goodOption.setIdUser(id);
    goodUser.setRole("client");
    Mockito.when(furnitureDAO.getOption(goodOption.getId())).thenReturn(goodOption);
    Mockito.when(furnitureDAO.getFurnitureById(goodOption.getIdFurniture()))
        .thenReturn(goodFurniture);
    Mockito.when(furnitureDAO.cancelOption(goodReason, goodOption.getId()))
        .thenReturn(goodOption.getIdFurniture());
    assertDoesNotThrow(() -> furnitureUCC.cancelOption(goodReason, goodOption.getId(), goodUser));
  }

  @DisplayName("Test to cancel an option when user is admin")
  @Test
  public void cancelOptionTest4() {
    int id = goodUser.getId() + 1;
    goodOption.setIdUser(id);
    Mockito.when(furnitureDAO.getOption(goodOption.getId())).thenReturn(goodOption);
    Mockito.when(furnitureDAO.getFurnitureById(goodOption.getIdFurniture()))
        .thenReturn(goodFurniture);
    Mockito.when(furnitureDAO.cancelOption(goodReason, goodOption.getId()))
        .thenReturn(goodOption.getIdFurniture());
    assertDoesNotThrow(() -> furnitureUCC.cancelOption(goodReason, goodOption.getId(), goodUser));
  }

  @DisplayName("Test to cancel an option with invalid option id")
  @Test
  public void cancelOptionTest5() {
    int id = goodUser.getId() + 1;
    goodOption.setIdUser(id);
    Mockito.when(furnitureDAO.getOption(goodOption.getId())).thenReturn(null);
    assertThrows(BusinessException.class,
        () -> furnitureUCC.cancelOption(goodReason, goodOption.getId(), goodUser));
  }

  @DisplayName("Test to get the furniture list with a null user")
  @Test
  public void getFurnitureListTest1() {
    List<FurnitureDTO> list = new ArrayList<FurnitureDTO>();
    list.add(goodFurniture);
    Mockito.when(furnitureDAO.getPublicFurnitureList()).thenReturn(list);
    List<FurnitureDTO> listB = furnitureUCC.getFurnitureList(null);
    assertAll(() -> assertEquals(list, listB), () -> assertEquals(1, listB.size()));
  }

  @DisplayName("Test to get the furniture list with a client user")
  @Test
  public void getFurnitureListTest2() {
    List<FurnitureDTO> list = new ArrayList<FurnitureDTO>();
    list.add(goodFurniture);
    goodUser.setRole(Role.CLIENT.toString());
    Mockito.when(furnitureDAO.getPublicFurnitureList()).thenReturn(list);
    List<FurnitureDTO> listB = furnitureUCC.getFurnitureList(goodUser);
    assertAll(() -> assertEquals(list, listB), () -> assertEquals(1, listB.size()));
  }

  @DisplayName("Test to get the furniture list with a non null user admin")
  @Test
  public void getFurnitureListTest3() {
    List<FurnitureDTO> list = new ArrayList<FurnitureDTO>();
    list.add(goodFurniture);
    Mockito.when(furnitureDAO.getFurnitureList()).thenReturn(list);
    List<FurnitureDTO> listB = furnitureUCC.getFurnitureList(goodUser);
    assertAll(() -> assertEquals(list, listB), () -> assertEquals(1, listB.size()));
  }

  @DisplayName("Test to get the furniture list with a null user")
  @Test
  public void getFurnitureListByTypeTest1() {
    List<FurnitureDTO> list = new ArrayList<FurnitureDTO>();
    list.add(goodFurniture);

    Mockito.when(furnitureDAO.getPublicFurnitureListByType(goodType.getId())).thenReturn(list);
    List<FurnitureDTO> listB = furnitureUCC.getFurnitureListByType(null, goodType.getId());
    assertAll(() -> assertEquals(list, listB), () -> assertEquals(1, listB.size()));
  }

  @DisplayName("Test to get the furniture list with a client user")
  @Test
  public void getFurnitureListByTypeTest2() {
    List<FurnitureDTO> list = new ArrayList<FurnitureDTO>();
    list.add(goodFurniture);
    goodUser.setRole(Role.CLIENT.toString());
    Mockito.when(furnitureDAO.getPublicFurnitureListByType(goodType.getId())).thenReturn(list);
    List<FurnitureDTO> listB = furnitureUCC.getFurnitureListByType(goodUser, goodType.getId());
    assertAll(() -> assertEquals(list, listB), () -> assertEquals(1, listB.size()));
  }

  @DisplayName("Test to get the furniture list with a non null user admin")
  @Test
  public void getFurnitureListByTypeTest3() {
    List<FurnitureDTO> list = new ArrayList<FurnitureDTO>();
    list.add(goodFurniture);
    Mockito.when(furnitureDAO.getFurnitureListByType(goodType.getId())).thenReturn(list);
    List<FurnitureDTO> listB = furnitureUCC.getFurnitureListByType(goodUser, goodType.getId());
    assertAll(() -> assertEquals(list, listB), () -> assertEquals(1, listB.size()));
  }

  @DisplayName("Test getting furniture by id with an invalid id")
  @Test
  public void getFurnitureByIdTest1() {
    int id = -5;
    Mockito.when(furnitureDAO.getFurnitureById(id)).thenReturn(null);
    assertThrows(BusinessException.class, () -> furnitureUCC.getFurnitureById(id));
  }

  @DisplayName("Test getting furniture by id with a valid id but no furniture has this id")
  @Test
  public void getFurnitureByIdTest2() {
    int id = 56;
    Mockito.when(furnitureDAO.getFurnitureById(id)).thenReturn(null);
    assertThrows(BusinessException.class, () -> furnitureUCC.getFurnitureById(id));
  }

  @DisplayName("Test getting furniture by id with valid id")
  @Test
  public void getFurnitureByIdTest3() {
    int id = goodFurniture.getId();
    photo1.setIdFurniture(goodFurniture.getId());
    Mockito.when(furnitureDAO.getFurnitureById(id)).thenReturn(goodFurniture);
    Mockito.when(furnitureDAO.getFavouritePhotoById(goodFurniture.getFavouritePhotoId()))
        .thenReturn(photo1.getPhoto());
    Mockito.when(furnitureDAO.getFurnitureTypeById(goodType.getId()))
        .thenReturn(goodType.getLabel());
    assertEquals(goodFurniture, furnitureUCC.getFurnitureById(id));
  }


  @DisplayName("Test getting furniture by id with a valid id seller")
  @Test
  public void getFurnitureByIdTest4() {
    int id = goodFurniture.getId();
    Mockito.when(furnitureDAO.getFurnitureById(id)).thenReturn(goodFurniture);
    assertEquals(goodFurniture.getSellerId(), furnitureUCC.getFurnitureById(id).getSellerId());
  }

  @DisplayName("Test getting furniture by id with valid id but no seller")
  @Test
  public void getFurnitureByIdTest5() {
    int id = goodFurniture.getId();
    goodFurniture.setSellerId(0);
    photo1.setIdFurniture(goodFurniture.getId());
    Mockito.when(furnitureDAO.getFurnitureById(id)).thenReturn(goodFurniture);
    Mockito.when(furnitureDAO.getFavouritePhotoById(goodFurniture.getFavouritePhotoId()))
        .thenReturn(photo1.getPhoto());
    Mockito.when(furnitureDAO.getFurnitureTypeById(goodType.getId()))
        .thenReturn(goodType.getLabel());
    assertEquals(goodFurniture, furnitureUCC.getFurnitureById(id));
  }

  @DisplayName("Test getting furniture by id with valid id but no idPhoto")
  @Test
  public void getFurnitureByIdTest6() {
    int id = goodFurniture.getId();
    goodFurniture.setFavouritePhotoId(0);
    photo1.setIdFurniture(goodFurniture.getId());
    Mockito.when(furnitureDAO.getFurnitureById(id)).thenReturn(goodFurniture);
    Mockito.when(furnitureDAO.getFavouritePhotoById(goodFurniture.getFavouritePhotoId()))
        .thenReturn(photo1.getPhoto());
    Mockito.when(furnitureDAO.getFurnitureTypeById(goodType.getId()))
        .thenReturn(goodType.getLabel());
    assertEquals(goodFurniture, furnitureUCC.getFurnitureById(id));
  }

  @DisplayName("Test to cancel a option after his term")
  @Test
  public void cancelOvertimedOptionsTest1() {
    assertDoesNotThrow(() -> furnitureUCC.cancelOvertimedOptions());
  }

  @DisplayName("Test get the list of the type of furniture")
  @Test
  public void getTypesOfFurnitureListTest1() {
    List<TypeOfFurnitureDTO> list = new ArrayList<TypeOfFurnitureDTO>();
    list.add(goodType);
    list.add(goodType);
    Mockito.when(furnitureDAO.getTypesOfFurnitureList()).thenReturn(list);
    List<TypeOfFurnitureDTO> listB = furnitureUCC.getTypesOfFurnitureList();
    assertAll(() -> assertEquals(list, listB), () -> assertEquals(2, listB.size()));
  }

  @DisplayName("Testing getting a photo of a furniture by an id, as an admin")
  @Test
  public void getFurniturePhotosTest1() {
    photo1.setId(3);
    photo2.setId(5);
    goodFurniture.setFavouritePhotoId(3);
    List<PhotoDTO> listA = new ArrayList<PhotoDTO>();
    listA.add(photo1);
    listA.add(photo2);
    int id = goodFurniture.getId();
    Mockito.when(furnitureDAO.getFurniturePhotos(id)).thenReturn(listA);
    Mockito.when(furnitureDAO.getFurnitureById(id)).thenReturn(goodFurniture);
    List<PhotoDTO> listB = furnitureUCC.getFurniturePhotos(id, goodUser);
    assertAll(() -> assertEquals(listA, listB), () -> assertEquals(2, listB.size()));
  }

  @DisplayName("Testing getting a photo of a furniture by an id, as a user")
  @Test
  public void getFurniturePhotosTest2() {
    photo1.setId(3);
    photo2.setId(5);
    photo1.setVisible(true);
    photo2.setVisible(true);
    goodFurniture.setFavouritePhotoId(3);
    goodUser.setRole(Role.CLIENT.toString());
    List<PhotoDTO> listA = new ArrayList<PhotoDTO>();
    listA.add(photo1);
    listA.add(photo2);
    int id = goodFurniture.getId();
    Mockito.when(furnitureDAO.getFurniturePhotos(id)).thenReturn(listA);
    Mockito.when(furnitureDAO.getFurnitureById(id)).thenReturn(goodFurniture);
    List<PhotoDTO> listB = furnitureUCC.getFurniturePhotos(id, goodUser);
    assertAll(() -> assertEquals(listA, listB), () -> assertEquals(2, listB.size()));
  }

  @DisplayName("Test get a photo of a furniture by an id, as a user, when the user is the seller")
  @Test
  public void getFurniturePhotosTest3() {
    photo1.setId(3);
    photo2.setId(5);
    photo1.setIsAClientPhoto(true);
    photo2.setIsAClientPhoto(true);
    photo1.setVisible(false);
    photo2.setVisible(false);
    goodFurniture.setFavouritePhotoId(3);
    goodUser.setRole(Role.CLIENT.toString());
    goodFurniture.setSellerId(goodUser.getId());
    List<PhotoDTO> listA = new ArrayList<PhotoDTO>();
    listA.add(photo1);
    listA.add(photo2);
    int id = goodFurniture.getId();
    Mockito.when(furnitureDAO.getFurniturePhotos(id)).thenReturn(listA);
    Mockito.when(furnitureDAO.getFurnitureById(id)).thenReturn(goodFurniture);
    List<PhotoDTO> listB = furnitureUCC.getFurniturePhotos(id, goodUser);
    assertAll(() -> assertEquals(listA, listB), () -> assertEquals(2, listB.size()));
  }

  @DisplayName("Test get a photo of a furniture by an id, as a user, when the photos are visibles")
  @Test
  public void getFurniturePhotosTest4() {
    photo1.setId(3);
    photo2.setId(5);
    photo1.setVisible(true);
    photo2.setVisible(true);
    goodFurniture.setFavouritePhotoId(3);
    goodUser.setRole(Role.CLIENT.toString());
    List<PhotoDTO> listA = new ArrayList<PhotoDTO>();
    listA.add(photo1);
    listA.add(photo2);
    int id = goodFurniture.getId();
    Mockito.when(furnitureDAO.getFurniturePhotos(id)).thenReturn(listA);
    Mockito.when(furnitureDAO.getFurnitureById(id)).thenReturn(goodFurniture);
    List<PhotoDTO> listB = furnitureUCC.getFurniturePhotos(id, goodUser);
    assertAll(() -> assertEquals(listA, listB), () -> assertEquals(2, listB.size()));
  }

  @DisplayName("Test get a furniture's photo by an id, as a user, when the user is ! the seller")
  @Test
  public void getFurniturePhotosTest5() {
    photo1.setId(3);
    photo2.setId(5);
    photo1.setIsAClientPhoto(true);
    photo2.setIsAClientPhoto(false);
    photo1.setVisible(false);
    photo2.setVisible(true);
    goodFurniture.setFavouritePhotoId(3);
    goodUser.setRole(Role.CLIENT.toString());
    goodFurniture.setSellerId(goodUser.getId() + 1);
    List<PhotoDTO> listA = new ArrayList<PhotoDTO>();
    listA.add(photo1);
    listA.add(photo2);
    int id = goodFurniture.getId();
    Mockito.when(furnitureDAO.getFurniturePhotos(id)).thenReturn(listA);
    Mockito.when(furnitureDAO.getFurnitureById(id)).thenReturn(goodFurniture);
    List<PhotoDTO> listB = furnitureUCC.getFurniturePhotos(id, goodUser);
    assertNotEquals(listA, listB);
  }

  @DisplayName("Test get a photo of a furniture by an id, as a user, when the user is the seller")
  @Test
  public void getFurniturePhotosTest6() {
    photo1.setId(3);
    photo2.setId(5);
    photo1.setIsAClientPhoto(false);
    photo2.setIsAClientPhoto(false);
    photo1.setVisible(false);
    photo2.setVisible(false);
    goodFurniture.setFavouritePhotoId(3);
    goodUser.setRole(Role.CLIENT.toString());
    goodFurniture.setSellerId(goodUser.getId());
    List<PhotoDTO> listA = new ArrayList<PhotoDTO>();
    listA.add(photo1);
    listA.add(photo2);
    int id = goodFurniture.getId();
    Mockito.when(furnitureDAO.getFurniturePhotos(id)).thenReturn(listA);
    Mockito.when(furnitureDAO.getFurnitureById(id)).thenReturn(goodFurniture);
    List<PhotoDTO> listB = furnitureUCC.getFurniturePhotos(id, goodUser);
    assertNotEquals(listA, listB);
  }


  @DisplayName("Test getting furniture with photo by an id with a null furniture")
  @Test
  public void getFurnitureWithPhotosByIdTest1() {
    int id = badFurniture.getId();
    Mockito.when(furnitureDAO.getFurnitureById(id)).thenReturn(null);
    assertThrows(BusinessException.class, () -> furnitureUCC.getFurnitureWithPhotosById(id));
  }

  @DisplayName("Test getting furniture with photo by id")
  @Test
  public void getFurnitureWithPhotosByIdTest2() {
    goodFurniture.setSeller(goodUser);
    goodFurniture.setSellerId(goodUser.getId());
    goodFurniture.setFavouritePhoto(photo1.getPhoto());
    goodFurniture.setTypeId(goodType.getId());
    goodFurniture.setType(goodType.getLabel());
    int sellerId = goodFurniture.getSellerId();
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    Mockito.when(userDAO.getUserFromId(sellerId)).thenReturn(goodUser);
    Mockito.when(furnitureDAO.getFavouritePhotoById(goodFurniture.getFavouritePhotoId()))
        .thenReturn(photo1.getPhoto());
    Mockito.when(furnitureDAO.getFurnitureTypeById(goodFurniture.getTypeId()))
        .thenReturn(goodType.getLabel());
    assertEquals(goodFurniture, furnitureUCC.getFurnitureWithPhotosById(goodFurniture.getId()));
  }

  @DisplayName("Test getting furniture with photo by id but without seller")
  @Test
  public void getFurnitureWithPhotosByIdTest3() {
    goodFurniture.setSeller(null);
    goodFurniture.setSellerId(0);
    goodFurniture.setFavouritePhoto(photo1.getPhoto());
    goodFurniture.setTypeId(goodType.getId());
    goodFurniture.setType(goodType.getLabel());
    int sellerId = goodFurniture.getSellerId();
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    Mockito.when(userDAO.getUserFromId(sellerId)).thenReturn(goodUser);
    Mockito.when(furnitureDAO.getFavouritePhotoById(goodFurniture.getFavouritePhotoId()))
        .thenReturn(photo1.getPhoto());
    Mockito.when(furnitureDAO.getFurnitureTypeById(goodFurniture.getTypeId()))
        .thenReturn(goodType.getLabel());
    assertEquals(goodFurniture, furnitureUCC.getFurnitureWithPhotosById(goodFurniture.getId()));
  }

  @DisplayName("Test getting furniture with photo by id but without idPhoto")
  @Test
  public void getFurnitureWithPhotosByIdTest4() {
    goodFurniture.setSeller(goodUser);
    goodFurniture.setSellerId(goodUser.getId());
    goodFurniture.setFavouritePhoto(null);
    goodFurniture.setFavouritePhotoId(0);
    goodFurniture.setTypeId(goodType.getId());
    goodFurniture.setType(goodType.getLabel());
    int sellerId = goodFurniture.getSellerId();
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    Mockito.when(userDAO.getUserFromId(sellerId)).thenReturn(goodUser);
    Mockito.when(furnitureDAO.getFavouritePhotoById(goodFurniture.getFavouritePhotoId()))
        .thenReturn(photo1.getPhoto());
    Mockito.when(furnitureDAO.getFurnitureTypeById(goodFurniture.getTypeId()))
        .thenReturn(goodType.getLabel());
    assertEquals(goodFurniture, furnitureUCC.getFurnitureWithPhotosById(goodFurniture.getId()));
  }

  @DisplayName("Test to process a list of visit")
  @Test
  public void processVisitTest1() {
    List<FurnitureDTO> list = new ArrayList<FurnitureDTO>();
    list.add(goodFurniture);
    list.add(goodFurniture);
    assertTrue(furnitureUCC.processVisit(list));
  }

  @DisplayName("Test getSliderFurnitureList with empty list")
  @Test
  public void getSliderFurnitureListTest1() {
    List<FurnitureDTO> list = new ArrayList<FurnitureDTO>();
    Mockito.when(furnitureDAO.getSliderFurnitureList(10)).thenReturn(list);
    assertEquals(list, furnitureUCC.getSliderFurnitureList(10));
  }

  @DisplayName("Test getSliderFurnitureList with one furniture")
  @Test
  public void getSliderFurnitureListTest2() {
    List<FurnitureDTO> listA = new ArrayList<FurnitureDTO>();
    listA.add(ObjectDistributor.getFurnitureForFurnitureUCCTest());
    Mockito.when(furnitureDAO.getSliderFurnitureList(10)).thenReturn(listA);
    List<FurnitureDTO> listB = furnitureUCC.getSliderFurnitureList(10);
    assertEquals(listA, listB);
  }

  @DisplayName("Test getSliderFurnitureList with ten furnitures")
  @Test
  public void getSliderFurnitureListTest3() {
    List<FurnitureDTO> listA = new ArrayList<FurnitureDTO>();
    for (int i = 0; i < 10; i++) {
      listA.add(ObjectDistributor.getFurnitureForFurnitureUCCTest());
    }
    Mockito.when(furnitureDAO.getSliderFurnitureList(10)).thenReturn(listA);
    List<FurnitureDTO> listB = furnitureUCC.getSliderFurnitureList(10);
    assertEquals(listA, listB);
  }

  @DisplayName("Test edit with empty furniture id")
  @Test
  public void editTest1() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    assertThrows(BusinessException.class, () -> furnitureUCC.edit(edition));
  }

  @DisplayName("Test edit w/ valid furn. id, and nothing else")
  @Test
  public void editTest2() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertTrue(furnitureUCC.edit(edition));
  }

  @DisplayName("Test edit w/ valid furn. id, a valid price of 0")
  @Test
  public void editTest3() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    edition.setOfferedSellingPrice(0);
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertTrue(furnitureUCC.edit(edition));
  }

  @DisplayName("Test edit w/ valid furn. id, a valid price of 100")
  @Test
  public void editTest4() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    edition.setOfferedSellingPrice(100);
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertTrue(furnitureUCC.edit(edition));
  }

  @DisplayName("Test edit w/ valid furn. id, an invalid negative price")
  @Test
  public void editTest5() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    edition.setOfferedSellingPrice(-1);
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertTrue(furnitureUCC.edit(edition));
  }

  @DisplayName("Test edit w/ valid furn. id, a valid id type of 1")
  @Test
  public void editTest6() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    edition.setIdType(1);
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertTrue(furnitureUCC.edit(edition));
  }

  @DisplayName("Test edit w/ valid furn. id, an invalid id type < 1 (0)")
  @Test
  public void editTest7() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    edition.setIdType(0);
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertTrue(furnitureUCC.edit(edition));
  }

  @DisplayName("Test edit w/ valid furn. id, an invalid id type < 1 (-1)")
  @Test
  public void editTest8() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    edition.setIdType(-1);
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertTrue(furnitureUCC.edit(edition));
  }

  @DisplayName("Test edit w/ valid furn. id, an id type similar to the actual furniture)")
  @Test
  public void editTest9() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    edition.setIdType(goodFurniture.getTypeId() + 1);
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertTrue(furnitureUCC.edit(edition));
  }

  @DisplayName("Test edit w/ valid furn. id, an empty endition")
  @Test
  public void editTest10() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    edition.setDescription("");
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertAll(() -> assertFalse(edition.getDescription().equals(goodFurniture.getDescription())),
        () -> assertTrue(furnitureUCC.edit(edition)));
  }

  @DisplayName("Test edit w/ valid furn. id, a valid description != to the original")
  @Test
  public void editTest11() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    edition.setDescription("Random description");
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertAll(() -> assertFalse(edition.getDescription().equals(goodFurniture.getDescription())),
        () -> assertTrue(furnitureUCC.edit(edition)));
  }

  @DisplayName("Test edit w/ valid furn. id, a valid description == to the original")
  @Test
  public void editTest12() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    edition.setDescription(goodFurniture.getDescription());
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertAll(() -> assertTrue(edition.getDescription().equals(goodFurniture.getDescription())),
        () -> assertTrue(furnitureUCC.edit(edition)));
  }

  @DisplayName("Test edit w/ valid furn. id, a valid favourite id")
  @Test
  public void editTest13() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    goodFurniture.setFavouritePhotoId(1);
    edition.setFavouritePhotoId(2);
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertTrue(furnitureUCC.edit(edition));
  }

  @DisplayName("Test edit w/ valid furn. id, an invalid favourite id")
  @Test
  public void editTest14() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    goodFurniture.setFavouritePhotoId(photo1.getId());
    edition.setFavouritePhotoId(-5);
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertAll(() -> assertTrue(furnitureUCC.edit(edition)),
        () -> assertNotEquals(edition.getFavouritePhotoId(), goodFurniture.getFavouritePhotoId()));
  }


  @DisplayName("Test edit w/ valid furn. id, an empty photo list to add")
  @Test
  public void editTest15() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    edition.setPhotosToAdd(new ArrayList<PhotoDTO>());
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertTrue(furnitureUCC.edit(edition));
  }

  @DisplayName("Test edit w/ valid furn. id, a non empty photo list to add but ! related")
  @Test
  public void editTest16() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    edition.setPhotosToAdd(new ArrayList<PhotoDTO>());
    photo1.setIdFurniture(goodFurniture.getId() + 1);
    edition.getPhotosToAdd().add(photo1);
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    Mockito.when(furnitureDAO.addAdminPhoto(photo1, goodFurniture.getId()))
        .thenReturn(photo1.getIdFurniture());
    assertThrows(BusinessException.class, () -> furnitureUCC.edit(edition));
  }

  @DisplayName("Test edit w/ valid furn. id, a non empty photo list to add related")
  @Test
  public void editTest17() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    edition.setPhotosToAdd(new ArrayList<PhotoDTO>());
    edition.getPhotosToAdd().add(photo1);
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    Mockito.when(furnitureDAO.addAdminPhoto(photo1, goodFurniture.getId()))
        .thenReturn(goodFurniture.getId());
    assertTrue(furnitureUCC.edit(edition));
  }

  @DisplayName("Test edit w/ valid furn. id, an empty photo list id to delete")
  @Test
  public void editTest18() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    edition.setPhotosToDelete(new ArrayList<Integer>());
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertTrue(furnitureUCC.edit(edition));
  }

  @DisplayName("Test edit w/ valid furn. id, a non empty photo list id to delete but ! related")
  @Test
  public void editTest19() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    edition.setPhotosToDelete(new ArrayList<Integer>());
    photo1.setIdFurniture(goodFurniture.getId() + 1);
    edition.getPhotosToDelete().add(photo1.getId());
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertThrows(BusinessException.class, () -> furnitureUCC.edit(edition));
  }

  @DisplayName("Test edit w/ valid furn. id, a non empty photo list id to delete related")
  @Test
  public void editTest20() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    edition.setPhotosToDelete(new ArrayList<Integer>());
    edition.getPhotosToDelete().add(goodFurniture.getFavouritePhotoId());
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    Mockito.when(furnitureDAO.deletePhoto(goodFurniture.getFavouritePhotoId()))
        .thenReturn(goodFurniture.getFavouritePhotoId());
    assertTrue(furnitureUCC.edit(edition));
  }

  @DisplayName("Test edit w/ valid furn. id, an empty photo list id to display")
  @Test
  public void editTest21() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    edition.setPhotosToDisplay(new ArrayList<Integer>());
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertTrue(furnitureUCC.edit(edition));
  }

  @DisplayName("Test edit w/ valid furn. id, a non empty photo list id to display but !related")
  @Test
  public void editTest22() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    edition.setPhotosToDisplay(new ArrayList<Integer>());
    photo1.setIdFurniture(goodFurniture.getId() + 1);
    edition.getPhotosToDisplay().add(photo1.getId());
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertThrows(BusinessException.class, () -> furnitureUCC.edit(edition));
  }

  @DisplayName("Test edit w/ valid furn. id, a non empty photo list id to display related")
  @Test
  public void editTest23() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    edition.setPhotosToDisplay(new ArrayList<Integer>());
    edition.getPhotosToDisplay().add(goodFurniture.getFavouritePhotoId());
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    Mockito.when(furnitureDAO.displayPhoto(goodFurniture.getFavouritePhotoId()))
        .thenReturn(goodFurniture.getFavouritePhotoId());
    assertTrue(furnitureUCC.edit(edition));
  }

  @DisplayName("Test edit w/ valid furn. id, an empty photo list id to hide")
  @Test
  public void editTest24() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    edition.setPhotosToHide(new ArrayList<Integer>());
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertTrue(furnitureUCC.edit(edition));
  }

  @DisplayName("Test edit w/ valid furn. id, a non empty photo list id to hide but ! related")
  @Test
  public void editTest25() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    edition.setPhotosToHide(new ArrayList<Integer>());
    photo1.setIdFurniture(goodFurniture.getId() + 1);
    edition.getPhotosToHide().add(photo1.getId());
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    assertThrows(BusinessException.class, () -> furnitureUCC.edit(edition));
  }

  @DisplayName("Test edit w/ valid furn. id, a non empty photo list id to hide related")
  @Test
  public void editTest26() {
    EditionDTO edition = ObjectDistributor.getEmptyEdition();
    edition.setIdFurniture(goodFurniture.getId());
    edition.setPhotosToHide(new ArrayList<Integer>());
    edition.getPhotosToHide().add(goodFurniture.getFavouritePhotoId());
    Mockito.when(furnitureDAO.getFurnitureById(goodFurniture.getId())).thenReturn(goodFurniture);
    Mockito.when(furnitureDAO.hidePhoto(goodFurniture.getFavouritePhotoId()))
        .thenReturn(goodFurniture.getFavouritePhotoId());
    assertTrue(furnitureUCC.edit(edition));
  }


  @DisplayName("Test getSliderFurnitureListByType with empty list")
  @Test
  public void getSliderFurnitureListByTypeTest1() {
    List<FurnitureDTO> list = new ArrayList<FurnitureDTO>();
    TypeOfFurnitureDTO type = goodType;
    Mockito.when(furnitureDAO.getSliderFurnitureListByType(10, type.getId())).thenReturn(list);
    assertEquals(list, furnitureUCC.getSliderFurnitureListByType(10, type.getId()));
  }

  @DisplayName("Test getSliderFurnitureListByType with one furniture")
  @Test
  public void getSliderFurnitureListByTypeTest2() {
    List<FurnitureDTO> listA = new ArrayList<FurnitureDTO>();
    TypeOfFurnitureDTO type = goodType;
    listA.add(ObjectDistributor.getFurnitureForFurnitureUCCTest());
    Mockito.when(furnitureDAO.getSliderFurnitureListByType(10, type.getId())).thenReturn(listA);
    List<FurnitureDTO> listB = furnitureUCC.getSliderFurnitureListByType(10, type.getId());
    assertAll(() -> assertEquals(listA, listB),
        () -> assertEquals(listB.get(0).getTypeId(), type.getId()));
  }

  @DisplayName("Test getSliderFurnitureListByType with ten furnitures")
  @Test
  public void getSliderFurnitureListByTypeTest3() {
    List<FurnitureDTO> listA = new ArrayList<FurnitureDTO>();
    TypeOfFurnitureDTO type = goodType;
    for (int i = 0; i < 10; i++) {
      listA.add(ObjectDistributor.getFurnitureForFurnitureUCCTest());
    }
    Mockito.when(furnitureDAO.getSliderFurnitureListByType(10, type.getId())).thenReturn(listA);
    List<FurnitureDTO> listB = furnitureUCC.getSliderFurnitureListByType(10, type.getId());
    assertAll(() -> assertEquals(listA, listB), () -> {
      for (int i = 0; i < 10; i++) {
        assertEquals(listB.get(i).getTypeId(), type.getId());
      }
    });
  }

  @DisplayName("Test getFurnitureListForResearch with empty list")
  @Test
  public void getFurnitureListForResearchTest1() {
    List<FurnitureDTO> list = new ArrayList<FurnitureDTO>();
    Mockito.when(furnitureDAO.getFurnitureListForResearch()).thenReturn(list);
    assertEquals(list, furnitureUCC.getFurnitureListForResearch());
  }

  @DisplayName("Test getFurnitureListForResearch with one furniture")
  @Test
  public void getFurnitureListForResearchTest2() {
    List<FurnitureDTO> listA = new ArrayList<FurnitureDTO>();
    listA.add(ObjectDistributor.getFurnitureForFurnitureUCCTest());
    Mockito.when(furnitureDAO.getFurnitureListForResearch()).thenReturn(listA);
    List<FurnitureDTO> listB = furnitureUCC.getFurnitureListForResearch();
    assertEquals(listA, listB);
  }

  @DisplayName("Test getFurnitureListForResearch with three furnitures")
  @Test
  public void getFurnitureListForResearchTest3() {
    List<FurnitureDTO> listA = new ArrayList<FurnitureDTO>();
    for (int i = 0; i < 3; i++) {
      listA.add(ObjectDistributor.getFurnitureForFurnitureUCCTest());
    }
    Mockito.when(furnitureDAO.getFurnitureListForResearch()).thenReturn(listA);
    List<FurnitureDTO> listB = furnitureUCC.getFurnitureListForResearch();
    assertEquals(listA, listB);
  }

}
