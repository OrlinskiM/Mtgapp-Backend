package com.magicapp.service;

import com.magicapp.domain.Guest;
import com.magicapp.repository.GuestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import static com.magicapp.constant.FileConstant.GUEST_IMAGE_PATH;

@Service
@Transactional
public class GuestService {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());
    private GuestRepository guestRepository;

    @Autowired
    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    public Guest addNewGuest(String firstName, String lastName, String username){
        Guest guest = new Guest(firstName, lastName, username, GUEST_IMAGE_PATH);
        return guestRepository.save(guest);
    }
}
