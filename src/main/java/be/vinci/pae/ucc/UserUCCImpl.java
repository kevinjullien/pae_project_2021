package be.vinci.pae.ucc;

import java.time.LocalDateTime;
import java.util.List;

import be.vinci.pae.domain.interfaces.FurnitureDTO;
import be.vinci.pae.domain.interfaces.SaleDTO;
import be.vinci.pae.domain.interfaces.User;
import be.vinci.pae.domain.interfaces.UserDTO;
import be.vinci.pae.exceptions.BusinessException;
import be.vinci.pae.exceptions.UnauthorizedException;
import be.vinci.pae.services.dal.DalServices;
import be.vinci.pae.services.dao.interfaces.AddressDAO;
import be.vinci.pae.services.dao.interfaces.FurnitureDAO;
import be.vinci.pae.services.dao.interfaces.SaleDAO;
import be.vinci.pae.services.dao.interfaces.UserDAO;
import be.vinci.pae.ucc.interfaces.UserUCC;
import jakarta.inject.Inject;

public class UserUCCImpl implements UserUCC {

  @Inject
  private UserDAO userDAO;

  @Inject
  private SaleDAO saleDAO;

  @Inject
  private FurnitureDAO furnitureDAO;

  @Inject
  private AddressDAO addressDAO;

  @Inject
  private DalServices dalServices;

  @Override
  public UserDTO connection(String email, String password) {
    dalServices.getBizzTransaction(true);
    User user = (User) userDAO.getUserFromEmail(email);
    dalServices.stopBizzTransaction();
    if (user == null) {
      throw new UnauthorizedException("Wrong credentials");
    } else if (!user.isValidated()) {
      throw new UnauthorizedException("User is not validated");
      // The password is checked after the validation to limit processor usage
    } else if (!user.checkPassword(password)) {
      throw new UnauthorizedException("Wrong credentials");
    }

    return user;

  }

  @Override
  public boolean registration(UserDTO user) {
    dalServices.getBizzTransaction(false);
    UserDTO u = userDAO.getUserFromEmail(user.getEmail());
    if (u != null) {
      throw new BusinessException("This email is already in use");
    }
    u = userDAO.getUserFromUsername(user.getUsername());
    if (u != null) {
      throw new BusinessException("This username is already in use");
    }

    user.getAddress().setId(addressDAO.addAddress(user.getAddress()));
    user.setPassword(((User) user).hashPassword(user.getPassword()));
    user.setValidated(false);
    user.setRegistrationDate(LocalDateTime.now());
    user.setRole("client");

    userDAO.addUser(user);

    dalServices.commitBizzTransaction();

    return true;
  }

  @Override
  public List<UserDTO> getUnvalidatedUsers() {
    dalServices.getBizzTransaction(true);
    List<UserDTO> list = userDAO.getUnvalidatedUsers();
    dalServices.stopBizzTransaction();
    return list;
  }

  @Override
  public boolean acceptUser(int id, String role) {
    if (role == null) {
      throw new BusinessException("Role is needed");
    }
    if (!role.equals("admin") && !role.equals("client") && !role.equals("antiquaire")) {
      throw new BusinessException("Invalid role");
    }

    dalServices.getBizzTransaction(true);
    boolean result = userDAO.acceptUser(id, role);
    dalServices.stopBizzTransaction();

    if (!result) {
      throw new BusinessException("Either the id is invalid or the user is already validated");
    }
    return result;
  }

  @Override
  public boolean deleteUser(int id) {
    dalServices.getBizzTransaction(true);
    boolean result = userDAO.deleteUser(id);
    dalServices.stopBizzTransaction();

    if (!result) {
      throw new BusinessException("Invalid id");
    }
    return result;
  }

  @Override
  public UserDTO getUserFromId(int id) {
    dalServices.getBizzTransaction(true);
    UserDTO user = userDAO.getUserFromId(id);
    dalServices.stopBizzTransaction();
    if (user == null) {
      throw new BusinessException("Invalid id");
    }
    return user;
  }

  @Override
  public List<UserDTO> getValidatedUsers() {
    dalServices.getBizzTransaction(true);
    List<UserDTO> list = userDAO.getValidatedUsers();
    dalServices.stopBizzTransaction();
    return list;
  }

  @Override
  public List<SaleDTO> getTransactionsBuyer(int id) {
    dalServices.getBizzTransaction(true);
    List<SaleDTO> list = saleDAO.getTransactionsBuyer(id);
    dalServices.stopBizzTransaction();
    return list;
  }

  @Override
  public List<FurnitureDTO> getTransactionsSeller(int id) {
    dalServices.getBizzTransaction(true);
    List<FurnitureDTO> list = furnitureDAO.getTransactionsSeller(id);
    dalServices.stopBizzTransaction();
    return list;
  }

}
