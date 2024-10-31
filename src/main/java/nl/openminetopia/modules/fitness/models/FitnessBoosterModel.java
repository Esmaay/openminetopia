package nl.openminetopia.modules.fitness.models;

import com.craftmend.storm.api.StormModel;
import com.craftmend.storm.api.enums.KeyType;
import com.craftmend.storm.api.markers.Column;
import com.craftmend.storm.api.markers.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@Table(name = "fitness_boosters")
public class FitnessBoosterModel extends StormModel {

    @Column(
            keyType = KeyType.FOREIGN,
            references = {FitnessModel.class}
    )
    private Integer fitnessId;

    @Column(name = "amount", defaultValue = "0")
    private Integer amount;

    @Column(name = "expires_at")
    private Long expiresAt;

    public boolean isExpired() {
        return expiresAt != -1 && System.currentTimeMillis() >= expiresAt;
    }
}
