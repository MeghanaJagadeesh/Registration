package com.planotech.plano.auth;


import com.planotech.plano.enums.AccountStatus;
import com.planotech.plano.enums.PlatformRole;
import com.planotech.plano.exception.InactiveException;
import com.planotech.plano.model.User;
import com.planotech.plano.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailService implements UserDetailsService {

    @Autowired
    UserRepository userRepo;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(username).orElseThrow(()->new UsernameNotFoundException("user not found"));
        if (user.getPlatformRole() == PlatformRole.ROLE_SUPER_ADMIN) {
            return new UserPrincipal(user);
        }
        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new InactiveException("User account is not active");
        }
        if (user.getCompany() != null && !user.getCompany().isActive()) {
            throw new InactiveException("Company is inactive");
        }

        return new UserPrincipal(user);
    }
}
