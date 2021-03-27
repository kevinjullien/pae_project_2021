package be.vinci.pae.domain.furniture;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import be.vinci.pae.api.exceptions.BusinessException;
import be.vinci.pae.api.exceptions.UnauthorizedException;
import be.vinci.pae.domain.address.Address;
import be.vinci.pae.domain.furniture.FurnitureDTO.Condition;
import be.vinci.pae.services.dal.DalServices;
import be.vinci.pae.services.dao.FurnitureDAO;
import jakarta.inject.Inject;

public class FurnitureUCCImpl implements FurnitureUCC {

  @Inject
  private FurnitureDAO furnitureDao;

  @Inject
  private DalServices dalServices;

  @Override
  public void indicateSentToWorkshop(int id) {
    dalServices.getConnection(false);
    Furniture furniture = (Furniture) furnitureDao.getFurnitureById(id);
    try {
      dalServices.commitTransactionAndContinue();
    } catch (SQLException e) {
      return;
    }
    if (furniture.getCondition().equals(Condition.ACHETE)) {
      furnitureDao.indicateSentToWorkshop(id);
      try {
        dalServices.commitTransaction();
      } catch (SQLException e) {
        dalServices.rollbackTransaction();
      }
    } else {
      throw new BusinessException("State error");
    }
  }

  @Override
  public void indicateDropOfStore(int id) {
    dalServices.getConnection(false);
    Furniture furniture = (Furniture) furnitureDao.getFurnitureById(id);
    try {
      dalServices.commitTransactionAndContinue();
    } catch (SQLException e) {
      return;
    }
    if (furniture.getCondition().equals(Condition.EN_RESTAURATION)
        || furniture.getCondition().equals(Condition.ACHETE)) {
      furnitureDao.indicateDropOfStore(id);
      try {
        dalServices.commitTransaction();
      } catch (SQLException e) {
        dalServices.rollbackTransaction();
      }
    } else {
      throw new BusinessException("State error");
    }
  }

  @Override
  public void indicateOfferedForSale(int id, double price) {
    dalServices.getConnection(false);
    Furniture furniture = (Furniture) furnitureDao.getFurnitureById(id);
    try {
      dalServices.commitTransactionAndContinue();
    } catch (SQLException e) {
      return;
    }
    if (price > 0 && furniture.getCondition().equals(Condition.DEPOSE_EN_MAGASIN)) {
      furnitureDao.indicateOfferedForSale(furniture, price);
      try {
        dalServices.commitTransaction();
      } catch (SQLException e) {
        dalServices.rollbackTransaction();
      }
    } else {
      throw new BusinessException("State error");
    }
  }

  @Override
  public void withdrawSale(int id) {
    // TODO Auto-generated method stub
    Furniture furniture = (Furniture) furnitureDao.getFurnitureById(id);

    if (furniture.getCondition().equals(Condition.EN_VENTE)) {
      furniture.setCondition("retiré de la vente");
    } else {
      throw new BusinessException("");
    }
  }

  @Override
  public void introduceOption(int optionTerm, int idUser, int idFurniture) {
    if (optionTerm <= 0) {
      throw new UnauthorizedException("optionTerm negative");
    }
    dalServices.getConnection(false);
    int nbrDaysActually = furnitureDao.getNumberOfReservation(idFurniture, idUser);
    try {
      dalServices.commitTransactionAndContinue();
    } catch (SQLException e) {
      return;
    }
    if (nbrDaysActually == 5) {
      throw new UnauthorizedException("You have already reached the maximum number of days");
    } else if (nbrDaysActually + optionTerm > 5) {
      int daysLeft = 5 - nbrDaysActually;
      throw new UnauthorizedException("You can't book more than : " + daysLeft + " days");
    } else {
      furnitureDao.introduceOption(optionTerm, idUser, idFurniture);
      try {
        dalServices.commitTransaction();
      } catch (SQLException e) {
        dalServices.rollbackTransaction();
      }
    }


  }

  @Override
  public void cancelOption(String cancellationReason, int idOption) {
    if (idOption < 1) {
      throw new BusinessException("Invalid id");
    }
    dalServices.getConnection(true);
    furnitureDao.cancelOption(cancellationReason, idOption);

  }

  @Override
  public List<FurnitureDTO> SeeFurnitureList() {
    // TODO Auto-generated method stub

    return null;
  }


  @Override
  public void introduceRequestForVisite(String timeSlot, Address address,
      Map<Integer, List<String>> furnitures) {
    // TODO Auto-generated method stub

  }



}
