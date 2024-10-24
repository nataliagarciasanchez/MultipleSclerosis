/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MultipleSclerosisJPA;

import MultipleSclerosisInterfaces.UserManager;
import MultipleSclerosisPOJOs.Role;
import MultipleSclerosisPOJOs.User;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 *
 * @author nataliagarciasanchez
 */
public class JPAUserManager implements UserManager {
    
    private EntityManager em;
	
	public JPAUserManager() {
		super();
		this.connect();
	}
    @Override
    public void newRole(Role role) {
	em.getTransaction().begin();
	em.persist(role);
	em.getTransaction().commit();	
    }
        
        
    @Override
    public void connect() {
        //TODO TIENE QUE COINCIDIR EL NOMBRE CON EL DEL PERSISTENCE.XML FILE DE META-INF
        em = Persistence.createEntityManagerFactory("multipleSclerosis-provider").createEntityManager();

	em.getTransaction().begin();
	em.createNativeQuery("PRAGMA foreign_keys = ON").executeUpdate();
	em.getTransaction().commit();
	
	if(this.getRoles().isEmpty())
	{
		Role admin = new Role("administrator");
		Role doctor = new Role("doctor");
		Role patient=new Role ("patient");
		this.newRole(admin);
		this.newRole(doctor);
		this.newRole(patient);
	
	}
    }

    @Override
    public void disconnect() {
        if (em != null && em.isOpen()) {
            em.close();  // Cierra el EntityManager
        }
    }

    @Override
    public void newUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        try {
            em.getTransaction().begin();  // Inicia una transacción
            em.persist(user);             // Persiste el objeto User en la base de datos
            em.getTransaction().commit(); // Hace commit a la transacción
        } catch (Exception e) {
            em.getTransaction().rollback(); // Si hay un error, realiza rollback
            throw new RuntimeException("Error al guardar el usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Role> getRoles() {
        List<Role> roles = null;
	    try {
	    	Query query = em.createNativeQuery("SELECT * FROM roles", Role.class);
	     roles = (List<Role>) query.getResultList();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return roles;
    }

    @Override
    public Role getRole(Integer id) {
        Role role=null;
		try {
			Query query = em.createNativeQuery("SELECT * FROM Roles WHERE id ="+id, Role.class);
			role = (Role) query.getSingleResult();
		}catch(NoResultException nre) {
			System.out.println("No role found with id: "+id);
		}catch(Exception e) {
			e.printStackTrace();
	}
		return role;
    }

    @Override
    public User getUser(String email) {
        User user=null;
	try {
	Query query = em.createNativeQuery("SELECT * FROM Users WHERE email ="+email, User.class);
	 user = (User) query.getSingleResult();
	}catch(NoResultException nre) {
		System.out.println("No user found with email: " + email);
	}catch(Exception e) {
		e.printStackTrace();
	}
	return user;
    }

    @Override
    public User checkPassword(String email, String password) {
        User user=null;
	try {
            Query query = em.createNativeQuery("SELECT * from Users where email = ? and password= ?", User.class);
			query.setParameter(1, email);
			MessageDigest md = MessageDigest.getInstance("MD");
			md.update(password.getBytes());
			
                        byte[] pw = md.digest();
			query.setParameter(2, pw);
			user = (User) query.getSingleResult();
			
	}catch(NoResultException e) {
            System.out.println("No user found with the provided email and password.");

	} catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(JPAUserManager.class.getName()).log(Level.SEVERE, null, ex);
        }
	
        return user;
	
    }

    @Override
    public void changePassword(User user, String new_password) {
        try {

            em.getTransaction().begin();
	    Query query = em.createNativeQuery("UPDATE Users SET password = ? WHERE id = ?");
	    MessageDigest md = MessageDigest.getInstance("MD");
            md.update(new_password.getBytes());
            byte[] new_Pw = md.digest();
            query.setParameter(1, new_Pw);
            query.setParameter(2, user.getId());
	       
	    query.executeUpdate();
	    em.getTransaction().commit();
            
	} catch (NoSuchAlgorithmException e) {
            if (em.getTransaction().isActive()) {
	        em.getTransaction().rollback();
	    }
	}
    }
    
   
    
}
