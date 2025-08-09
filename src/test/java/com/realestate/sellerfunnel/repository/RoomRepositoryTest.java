package com.realestate.sellerfunnel.repository;

import com.realestate.sellerfunnel.model.Room;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    private Room room(String number, String type, double rate, boolean vacant, boolean active) {
        Room r = new Room(number, null, type, BigDecimal.valueOf(rate));
        r.setIsVacant(vacant);
        r.setIsActive(active);
        return r;
    }

    @Test
    @DisplayName("Counts vacant and occupied rooms")
    void countsVacantAndOccupied() {
        roomRepository.save(room("101","Single",100,true,true));
        roomRepository.save(room("102","Single",100,false,true));
        roomRepository.save(room("103","Suite",250,false,true));
        roomRepository.save(room("104","Suite",250,true,false)); // inactive

        Long vacant = roomRepository.countVacantRooms();
        Long occupied = roomRepository.countOccupiedRooms();

        assertThat(vacant).isEqualTo(1L); // only 101 (104 inactive)
        assertThat(occupied).isEqualTo(2L); // 102,103
    }

    @Test
    @DisplayName("Exists by room number unique constraint")
    void existsByRoomNumberWorks() {
        roomRepository.save(room("200","Single",120,true,true));
        assertThat(roomRepository.existsByRoomNumber("200")).isTrue();
        assertThat(roomRepository.existsByRoomNumber("201")).isFalse();
    }

    @Test
    @DisplayName("Search term matches number and type")
    void searchTermMatches() {
        roomRepository.save(room("301","Single",90,true,true));
        roomRepository.save(room("401","Deluxe",180,true,true));
        List<Room> result = roomRepository.findBySearchTerm("Deluxe");
        assertThat(result).extracting(Room::getRoomNumber).containsExactly("401");
    }
}
