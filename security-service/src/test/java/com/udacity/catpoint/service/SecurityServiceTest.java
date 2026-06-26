package com.udacity.catpoint.service;


import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import java.util.HashSet;
import java.util.Set;
import com.udacity.catpoint.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SecurityServiceTest {

    @Mock
    private SecurityRepository securityRepository;

    @Mock
    private ImageService imageService;

    private SecurityService securityService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);

        securityService = new SecurityService(securityRepository, imageService);
    }

    @Test
    void armingSystemShouldSetArmingStatus() {

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        verify(securityRepository).setArmingStatus(ArmingStatus.ARMED_HOME);
    }

    @Test
    void disarmingSystemShouldResetAlarm() {

        securityService.setArmingStatus(ArmingStatus.DISARMED);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);

    }

@Test
void ifAlarmIsArmedAndSensorActivatedSetPendingAlarm() {

    Sensor sensor = new Sensor("Door", SensorType.DOOR);

    when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
    when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);

    securityService.changeSensorActivationStatus(sensor, true);

    verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
}

@Test
void ifAlarmIsPendingAndSecondSensorActivatedSetAlarm() {

    Sensor sensor = new Sensor("Door", SensorType.DOOR);

    when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
    when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

    securityService.changeSensorActivationStatus(sensor, true);

    verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
}

@Test
void ifPendingAlarmAndSensorDeactivatedReturnToNoAlarm() {

    Sensor sensor = new Sensor("Door", SensorType.DOOR);
    sensor.setActive(true);

    when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

    securityService.changeSensorActivationStatus(sensor, false);

    verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
}

@Test
void ifAlarmAlreadyActiveSensorChangesDoNotAffectAlarm() {

    Sensor sensor = new Sensor("Door", SensorType.DOOR);

    when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

    securityService.changeSensorActivationStatus(sensor, true);

    verify(securityRepository, never()).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    verify(securityRepository, never()).setAlarmStatus(AlarmStatus.NO_ALARM);
}

@Test
void ifSecondSensorActivatedWhilePendingAlarmThenAlarm() {

    Sensor sensor1 = new Sensor("Door", SensorType.DOOR);
    Sensor sensor2 = new Sensor("Window", SensorType.WINDOW);

    sensor1.setActive(true);

    when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
    when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

    securityService.changeSensorActivationStatus(sensor2, true);

    verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
}

@Test
void ifInactiveSensorSetInactiveAgainThenNoAlarmChange() {

    Sensor sensor = new Sensor("Door", SensorType.DOOR);
    sensor.setActive(false);

    securityService.changeSensorActivationStatus(sensor, false);

    verify(securityRepository, never()).setAlarmStatus(any());
}

@Test
void ifCatDetectedWhileArmedHomeThenAlarm() {

    when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
    when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

    securityService.processImage(null);

    verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
}

@Test
void ifNoCatDetectedWhileArmedHomeThenNoAlarm() {

    when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
    when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);

    securityService.processImage(null);

    verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);

}

@Test
void ifSystemDisarmedThenAlarmShouldBeNoAlarm() {

    securityService.setArmingStatus(ArmingStatus.DISARMED);

    verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    verify(securityRepository).setArmingStatus(ArmingStatus.DISARMED);

}

@Test
void whenSystemArmedAllSensorsShouldBeDeactivated() {

    Sensor sensor1 = new Sensor("Door", SensorType.DOOR);
    Sensor sensor2 = new Sensor("Window", SensorType.WINDOW);

    sensor1.setActive(true);
    sensor2.setActive(true);

    Set<Sensor> sensors = new HashSet<>();
    sensors.add(sensor1);
    sensors.add(sensor2);

    when(securityRepository.getSensors()).thenReturn(sensors);

    securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

    assertFalse(sensor1.getActive());
    assertFalse(sensor2.getActive());

    verify(securityRepository).updateSensor(sensor1);
    verify(securityRepository).updateSensor(sensor2);
}

}
