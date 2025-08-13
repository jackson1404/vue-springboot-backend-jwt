/***************************************************************
 * Author       :	 
 * Created Date :	
 * Version      : 	
 * History  :	
 * *************************************************************/
package com.jackson.vue.jwt_backend_integrate.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * PasswordResetToken Class.
 * <p>
 * </p>
 *
 * @author
 */
@Entity
@Table(name = "tbl_password_reset_tokens")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetTokenEntity {

    @Id
    @GeneratedValue
    private Long passwordResetId;

    private String tokenHash;

    private LocalDateTime expiryTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private Boolean used;

}
