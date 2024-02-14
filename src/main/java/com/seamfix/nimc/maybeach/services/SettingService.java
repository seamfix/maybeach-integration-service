package com.seamfix.nimc.maybeach.services;

import com.seamfix.nimc.maybeach.entities.Setting;
import com.seamfix.nimc.maybeach.enums.SettingsEnum;
import com.seamfix.nimc.maybeach.repositories.SettingRepository;
import com.seamfix.nimc.maybeach.utils.AppProperties;
import com.seamfix.nimc.maybeach.utils.SettingsCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingService {

    private final SettingsCache settingsCache;
    private final SettingRepository settingRepository;
    private final AppProperties appProperties;

    public String getSettingValue(SettingsEnum settingsEnum) {

        return getSetting(settingsEnum.getName(), settingsEnum.getValue(), settingsEnum.getDescription());
    }


    private String getSetting(String settingsName, String defaultValue, String description) {

        String val = settingsCache.getItem(settingsName, String.class);

        if (val != null) {
            return val;
        }
        Setting setting = settingRepository.findByName(settingsName)
                .orElseGet(() -> {
                    Setting newSetting = new Setting();
                    newSetting.setName(settingsName);
                    newSetting.setValue(defaultValue);
                    newSetting.setDescription(description);
                    return settingRepository.save(newSetting);
                });

        val = setting.getValue();

        settingsCache.setItem(settingsName, val, appProperties.getSettingsCacheTime());

        return val;
    }
}
