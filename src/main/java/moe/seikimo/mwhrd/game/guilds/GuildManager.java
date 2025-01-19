package moe.seikimo.mwhrd.game.guilds;

import moe.seikimo.data.DatabaseUtils;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.models.BasicPlayerInfo;
import moe.seikimo.mwhrd.utils.Maps;
import moe.seikimo.mwhrd.utils.PlayerList;
import moe.seikimo.mwhrd.utils.Players;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

import java.util.*;

public final class GuildManager {
    /**
     * A set of all possible guild colors.
     */
    public static final Set<Formatting> GUILD_COLORS = Set.of(
        Formatting.RED,
        Formatting.GOLD,
        Formatting.YELLOW,
        Formatting.GREEN,
        Formatting.AQUA,
        Formatting.BLUE,
        Formatting.LIGHT_PURPLE,
        Formatting.WHITE
    );

    /**
     * Maps Minecraft formatting colors to a pair of icon heads and their signatures.
     */
    private static final Map<Formatting, Pair<String, String>> GUILD_ICONS = Maps.guildIcons()
        .put(Formatting.RED, new Pair<>(
            "eyJ0aW1lc3RhbXAiOjE1ODMzNDM0MDQzNTIsInByb2ZpbGVJZCI6IjNmYzdmZGY5Mzk2MzRjNDE5MTE5OWJhM2Y3Y2MzZmVkIiwicHJvZmlsZU5hbWUiOiJZZWxlaGEiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzYzZjc5YjIwN2Q2MWUxMjI1MjNiODNkNjE1MDhkOTljZmEwNzlkNDViZjIzZGYyYTlhNTEyN2Y5MDcxZDRiMDAifX19",
            "j0vUZ9EUukdIOQ630W8iGOFO2y3KZjWwLKR+v9tYmap4+ZEcyvBCdEXoESLMTSQe3hJuf/4NtrhfZPdHGBCj3WDZFh3b78L7nVM9RLqLBCE3s1ZN85l9kddJD/i/o8LS28PAvAchcxlMmNGskLnDXFQFeyMOsMW7RW+xj+06zRYTOl5Q40syRP1CjK2HZW4WVTpbpLMFXk25633DmfTWhYE6ur0jKGhT1Mv6OXC9aQKjMclBod+bXp0N3ItLZVhBSExUnWb1tRxH64eV7uzKx+eTy4K1Eee2kluLFvCPuP4WPlWMOiD+sA7tnFi7/PfQ1RZ+2SxFMsG3yx3GBBb0RBKPcBoWvdKyl+i2DBRrWxq6cbbBySJRIV/inADNgveIulXzqjHZxCn/ZVTd+IMXadvJKgP+Ki7gT3Ik6C1PMfFYf92bsjgU4CxRO/az51rjz2QJhZZ690XFgTA5QJec7Hj8FcboCVfOWPOiPhCnJP/hApk65UvgRGvqfFIGNyZoTmIAWxRxdLutOHDVs+uRbhWnO/JjC+Mh57weyDYzXBD3hrWlmddg1YSk4ayOwFDlm9ObyM9D0JsSurzVfbw5z7X6oiY0xKM6/kBn4a6FAkUVW5w3BBwcxsbWxyxv5IJqWooITaAot/T8TeyEWuR8vTwSiYpgcelmWjNTjKf5Uu0="
        ))
        .put(Formatting.GOLD, new Pair<>(
            "ewogICJ0aW1lc3RhbXAiIDogMTczNTUxMTE1ODM1NCwKICAicHJvZmlsZUlkIiA6ICI0M2NmNWJkNjUyMDM0YzU5ODVjMDIwYWI3NDE0OGQxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJrYW1pbDQ0NSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yMDkwZDA5ZTE3M2VlMzQxMzhjM2IwMWI0OGVlMGJlNTM0YmJiMWFjZTBkZGY1ZmY5OGU2NmY3YjAyMTEzOTk1IgogICAgfQogIH0KfQ==",
            "F7a3dz7mxVy4hcFPAoShPQOjR8d14yWG8ewY9VC3jR/3vNdLjaBgvcswWoI/yxwVTSSl5ZcqMCYCPikHahbnVSLqt+hOOauCl9Kf1oKXgS/EjFd3AqCPDIgBj2zIVyKqQ76z1gd64LCXYOzeclNUVm4k6IA36VvkQY7c9ztRerpWK0PhBxrByMg1sXFYWMoWD8iRYDEhOv4kJz6e30A+yelrewveX/a0U7VCXqg7o0e5DBdzn1II+ch9ua6ZbJQ+MCEjL6C/TW6qDVknp5bNvOCcyldt4SVzCod/QeNAO0DRsUzTUFTbtQOdXgPVNNIvnc6ZoFUs8UynoE49bLa9b9GockWkkfyuwvzuYRtNYZYl61hJPFCuhrn/6u6tiRi8CbQBZPvPgj2w4s0EpqOZHyuDNJA2t2CabLLgR7meb9oedGsvaF2BAtrCgCUVIwwzcS1t0CFdFOFZ3GGlCnasTTpYiXxP8zmxK1rpH7ypO6zBbFuAgOJ+TCST321S9XgIdjg0xqvKBaPAoyyKJwifnNUW2FdduE5BovxH34Sq5XMUReCB4+gX+mVEc2N/IO+h4ck/yv+8FEgjvq86PhMg/kOVUMKHa88UmHSLZMwdLbw+FNuDnMct+4J6lA/VINR6LJ5iY+4580cAMhRGSGahayMBpcTi4bpWajISqqqg+jQ="
        ))
        .put(Formatting.YELLOW, new Pair<>(
            "ewogICJ0aW1lc3RhbXAiIDogMTczNjEwMDI2NjgwMywKICAicHJvZmlsZUlkIiA6ICI5ZjJiY2M1M2U4YzM0OTY4YTc5Yzc0NTExYWQ2NmQyYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJLYWJveWlvIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2E4OWI5M2ZkNjE2ZWQzNjcwY2NmNjQ3YTBmOTM4MDM5OGMwZDQ2MTU2MzRmMmRlZmY0NmM2ZWRiZGM3MTI4ODUiCiAgICB9CiAgfQp9",
            "Do4IByDR1YEhlWyvF++CDOSaqKad4slyoOqlB++bGNKxOzTLqw+Z1SXMQOluJE5svKpIlK3Ewcc1ilHDoMEcYSkMckIHQslk1Tq9BHZl2C8x8Ukdmk8euGQ0d3nYSDfMQp3NF9OuQOnKCGEB/9DWE0NCui8mYCPpq6z1Wcg45kuu2QnYblaRuPDncpXsB00NJHLAtQxFgroqzI6yD4sNR1etB87CJNH0b7dOA2pEKBWnb2jhxsQjPQ6FissTwH6V89ZcJJZzOOiVDZKsLEJGZ/u+pSEePyVN0mA+otTEWuryvbzFZYVgupjLmz9AZ9wKm44K+gy5YQMnhiszLav+beTbck+KrENOmdpYyx4rpYcCKQw2QTrBZ6c1jCQz+qJNKhcMlMsO2Su9U3dYWTsXvtkrIJ9UPoR3hBuPxjsU7qWPXPaMZaXTFcyNdHAMFPu8p1KROPuBqiC4e1hJEMtlOT6uOpxQWixkXZm5flqRRE6RvzbQk0cIJxaHnNhdIstYFxjFGrvsz4yIbEQcHiMQ/64C2teh8qgeIpUnSAequV9M+rqJgGyw0TNlYk54buMm1h0CUKg0vxaX/vzskKJMFM/n5f9jucRKCYD/vK/g/EBzVnIKvQVeMGofdgcNuxgxm8UFC6kmUE09oM3zvCfbR25JSdrH8LrWZgQDWspnaCs="
        ))
        .put(Formatting.GREEN, new Pair<>(
            "ewogICJ0aW1lc3RhbXAiIDogMTY4NzU0MTA1NTI4NSwKICAicHJvZmlsZUlkIiA6ICJmYzFhOTdlNTgxM2Y0NDI2YTNmZTI4ZjJiNDc1ZjA4ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJnZXRPbmxpbmVQbGF5ZXJzIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzkyNWI4ZWVkNWM1NjViZDQ0MGVjNDdjNzljMjBkNWNmMzcwMTYyYjFkOWI1ZGQzMTAwZWQ2MjgzZmUwMWQ2ZSIKICAgIH0KICB9Cn0=",
            "BbUvvUZbjJs56MGj1YyUIIj69EfMxpKuymOQjW4cOSdfOsziavrLG6tnRVr/NPfdumBHhZJb0h/G2U89GwohkbU6I6SntAYNur4B+MRVjsl7UoxBxS03rsVoILTJaOUP5qK6x5gsuFUfnXNAY3swJ4S5r5R9z2rGaoTW0XogO66EmD3SvGAEBVAQBhA5cj3k7POo0xe1+wb/WlFw6oKiY2hXYzZW6TOjtuysa2tJHkP+fyOY46QhO5CjkAvBZBPHD05WwXi+cvqlKodhnlGzhXN5FJ5qeluFwUf94CuNwyncG8UJGXsmvYmo7SHQP9OL81YdDYJj65cHoGMUkKPmXr//w4hus9ZYlr6yu8i0J1bolAteX00RxhMyz6JvPYFusv7OHBrlvQSpcywwsC4L3oihkGrdLRXFcBSic+TLrvutw01NQiIPWtdSStjFfzDaTOGNWeC70VkRMiI7/AUyicPK+baejKIVgPKk3l2NiiF/D2LA8LDiXLlKABmbPr+Xa96wElE04ZaA2nvnshJ1QbTa7L/Way1Ra/6D5cAKv2yTz7Q+0OHNLP/CQwyzrLtke5/YIV7SU5EOa8O0vJMeuq9zSvZPMGVIZFo6ba9jGnGpeMzTSH9XU3Ygnt+pVEI4duh9t4l0FmDcab5hNxaLUGrcroD9dlRDxxVcHCFTK9Q="
        ))
        .put(Formatting.AQUA, new Pair<>(
            "ewogICJ0aW1lc3RhbXAiIDogMTczNTUxODA0NTEzMiwKICAicHJvZmlsZUlkIiA6ICJhYWMxYjA2OWNkMjE0NWE2ODNlNzQxNzE4MDcxMGU4MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJqdXNhbXUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmQ2YThiNDdkYTkyM2I3ZDEwMTQyNDQ3ZmRiZGNmZDFlOGU4MmViNDg0OTY0MjUyYmIzNmRkYjVmNzNiNTFjMiIKICAgIH0KICB9Cn0=",
            "oWMW9i8DuIB5dzKkgZy92rokGYkGdSCYD6wl7NPrmoHHCoaKgRa6aNqr+i2GYmeDS7Jm8wzQ6d6H+osDbGaMFoOEBiCWrAjubTbYa/6jxxN1OzNBCD8wKGQyiGng8nOfBNQKZpmxdWVsWiHp7Q2vRimxBOtkcrN+Ozl9+AAQpAN89p5CeEmttaJWf6Vv31ElDN623Zz5O+NbRsBjiC342t1fr746QgSgdAgw7Z75DMF1pMROOunKc34g/9AWRPMl/RnWl23m1pHUI4bvZGZA4LPveGnnx9NRfX4Mc4h2uFn/ySS6qlUMgcE0qktOxCsGPkPEZicX+KCYwfoXHROTDKIs3wQFZ4VIsSaEeODAnaKWV5hJlurxgoZBdBlhxvZ9WEuK1Fjqc23tOz2EIkLLoHkjSc5+b6FeshBrgNQ8mgZmjU3zfRMVpxFXCKdCKWgSHeuHYoMiUQBKss9YHtC99TkYTnZj94CNpOJHqJu4Ix3q28UUfrrxKzIrhu/wZdYHdUfwXpXDzy2iB2+eqZPFFWilWyP3p1oRjfrkbTgYs6dxNAdAeHpqOShPTRezCHw/wWwDMh2l54EjUiz58ozHyvZP1U6Ggi28usJcM5BH15utxJnJJbkOL/dUnA8Pkl3UEa7oOjR/QUo/njJOEtfbKxDZUrwRTi1JHEDZ8A+yo6I="
        ))
        .put(Formatting.BLUE, new Pair<>(
            "ewogICJ0aW1lc3RhbXAiIDogMTczNjEwMDYyMTY3OCwKICAicHJvZmlsZUlkIiA6ICJlODE1MGY1MjlmZGU0YzdkYjI3OTAyZjJjNmU3NTc5ZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJCcmFkQm90XzIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2Y5YzMyMTM4Yzk3NjRjNjM5YWViZDgxOWNkOTE5OTJhZWQwMWJmNDQ4ZjBlNzEwYTAzYWI0NDNhYzQ5MGVlOSIKICAgIH0KICB9Cn0=",
            "CCr+98zyIO5VbJMZd+8T4G0TwOUYw84MYab17JKB3laBOwlpChhGzdFdergboP4Ksy8jCuqIo28d37/mI1IbWrm1saQMafGAzJxMifCTKTPLbax9GmFkSC6JGOhwL7j0e/4Ts9zOgdqg3NwSr3Pn0I5vX9fU2HKSEGizQv3RR/3ot6qJsslF2ar7cxkmb8fx5mpS7LsW5FbDGk7yKyKzFMFgTHkB+k9+Nv3bYoYoYr7ieB6TI9824D3G4NcLEmyAkoZE5ZuWQJz3cyveu156sr8WCgnOcE0AQAANMh3XEDIPvXg9PahlcVt9cyYLzLn8x1aRYGOr0o9Wxpghdns+vvKLIv0wMeTfuN+M0RedSgbdw893aaOJN5FfCITHqkbKv6Y4JAv5uwpNATbU/DrRV2qKAj0dvUgtr5WDt3dYv1NEWk+3vvYLRll6zqgvVB2zDFsSDKBSpaXNxYgVgiXmQI1BeVDVTJ+A1w2fwhDuL7AUenyPVxQxP08sr/9+LxwaeInp1Zl/DP2YeY512/9mx6vubfa8G7z5QPEluPDx+xz2H/l3PrC7DHBYl0uVT38sEf/clcsXDpIo1Rv1zlFqLU4ermMPKUVagVObsMgbLBpa1XzlznzIwG3TZq/WV4JKE0kos3nC1MSM9bddjLlYXpBo+Njx5xlPazaYsjomrbY="
        ))
        .put(Formatting.LIGHT_PURPLE, new Pair<>(
            "ewogICJ0aW1lc3RhbXAiIDogMTczNjEwMDY3Nzg1NSwKICAicHJvZmlsZUlkIiA6ICI2OTE1MGMxMTk3M2E0MGViOGExNjZiNTY5OThmNWEzMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJMeXgyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RjZjI4MzUxODBjYmZlYzNiMzE3ZDZhNDc0OTFhNzRhZTcxNDM1YmExNjlhNTc5MjViOTA5NmVhMmY5YzYxYjYiCiAgICB9CiAgfQp9",
            "vKccpwG33tzowOeYZMqpIT8t5HJFrXtyde8799oqewNpDndhc5iUk3ANQNhOZaUa7ahj8rwpSQTqxYPQL1/xqLtWubBO4qcHdWiPi2RRF6nPRMr3Lk6KPEGpaR4ZVUAK4RZhPPzj1DhVp0I0ovHecDmQgr/j3q6pF9diuMoF4ydAnGQIeINgvumgcnUZfpIOBtL59udvipCqu2mSbJz7Gm5dFv63XYmSN6qHl5hvCvWcBTMatagQp+9Caj2xrGGWaAxmtUarliQargqmglz0sL9GbIgEUSlhvpMFGchG25/lkWYWKF1TkEv9kM5LzFhDXb0bt6ORF3w+l98S+GpU9qbbfThh4Pq7rYNMQj5B7naf2lHI44MwXnNz7afp/pqtJROu71ySLKW0RTpLI43OQ01ZtFe0DLyKUfFU9qbLYbNT+x3l6eKksxO1vDZsRJtj4UZCiB1KLEltU0riksHsndeBKyggXhLsItd8+2mNoJ1gBuxJo+vqnD+aYPZ2ydBlU3v2gR/I4eo/m7SJnRBtQxF5t0ceT1hggADnytrmoOSHS1Q8VFj8MP50JOJvVlRhrqwF6E81thidWlhXE+9klv48Hvcizn76YuUcSGhzU/MsO4144sAC1/doraGKZgW1t3/+slU2UEy3tS8tWruH13qLu2bHsScKaRdgLSQ7abE="
        ))
        .put(Formatting.WHITE, new Pair<>(
            "ewogICJ0aW1lc3RhbXAiIDogMTcyMDAxNjE5MzA0NSwKICAicHJvZmlsZUlkIiA6ICI3ZGEyYWIzYTkzY2E0OGVlODMwNDhhZmMzYjgwZTY4ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJHb2xkYXBmZWwiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmYxYTUzYmQwNjg1MjBjZjY5ZTcxM2RhZTk3YWQzMDU5N2JkMzQ1OGZhMGU3OTFiYmEwNTJjZTcwZTBhZWM0NSIKICAgIH0KICB9Cn0=",
        "vDl5bSmar9+4dtOMO2LO2VQ+ZsXwaQwkbRlky63TlX/H9TASqf5pH/Vj7KxPgm0pzYSSOwhz027bMaA4aEKLxRVkT44lTFkvu3mYB7oSg7U1GTOmrgbBzq3LH+ty7g+/6QTux9p6+z8GuiVGaxB+CaDqU06Kz4/YhbASUhzvEmgp5VNANisxQsPcKNQgkRjG4ibal2i3zEY1LFLquvfQtS9L2D3sdoztHnrSo0ZWHCrJvMf2UyKdbWYkv84fih3cVXmUBaQvxJDspvkciwRepNRZtH0zNc1HbdRux+AD2XAhfQ9qim8T6LSyvZc8kr3qrmRgL+M3DOBUrgjA6oBHAhPempJ4mO38nCn8+odUD4K9nTIoUGsE9h86HPv6LnfK+0F1aaILsmLzDLi63NUv8di2m8TNCyXZbLCwzVTaEiNnc2Hs9crWFQSN9vo5fFlsLwrjmyz8NZL/Psyf77c0/1Ap4yRIE3ggXLPu8+DQ6QiyrMZVmsYrGl6skaakDjWwdrT8yzWYkC1SGZurBE9/rlywvvEPztrCS78GH0rTo/hJAGzzF8kNnxajfoJqHBO3uBeHeOkURLr6G5UsCzIyrEONBQ+d9LU/RXo9c87z7MT4hapxVvE72vcboRdifrMbBdaw+N0ldagomv9zGVAJw7bq94PAsEq/D0Zyqci8t80="
        ))
        .build();

    /**
     * Maps Minecraft formatting colors to their respective indexes in the player list.
     */
    private static final Map<Formatting, Integer> GUILD_INDEXES = Maps.guildIndexes()
        .put(Formatting.RED, 79)
        .put(Formatting.GOLD, 59)
        .put(Formatting.YELLOW, 39)
        .put(Formatting.GREEN, 19)
        .put(Formatting.AQUA, 69)
        .put(Formatting.BLUE, 49)
        .put(Formatting.LIGHT_PURPLE, 29)
        .put(Formatting.WHITE, 9)
        .build();

    /**
     * Maps Minecraft formatting colors to their respective ranges in the player list.
     */
    private static final Map<Formatting, Pair<Integer, Integer>> GUILD_RANGES = Maps.guildRanges()
        .put(Formatting.RED, new Pair<>(70, 78))
        .put(Formatting.GOLD, new Pair<>(50, 58))
        .put(Formatting.YELLOW, new Pair<>(30, 38))
        .put(Formatting.GREEN, new Pair<>(10, 18))
        .put(Formatting.AQUA, new Pair<>(60, 68))
        .put(Formatting.BLUE, new Pair<>(40, 48))
        .put(Formatting.LIGHT_PURPLE, new Pair<>(20, 28))
        .put(Formatting.WHITE, new Pair<>(0, 8))
        .build();

    /**
     * Maps Minecraft formatting colors to guild instances.
     */
    private static final Map<Formatting, GuildInstance> GUILDS = new HashMap<>();

    /**
     * Creates guild instances by color.
     */
    public static void initialize() {
        for (var color : GUILD_COLORS) {
            // Load the guild from the database.
            var instance = DatabaseUtils.fetch(
                GuildInstance.class,
                "_id", color.getColorIndex()
            );

            // If it doesn't exist, make a new instance.
            if (instance == null) {
                instance = new GuildInstance(color);
            }

            GUILDS.put(color, instance);
        }
    }

    /**
     * Gets a guild instance by color.
     *
     * @param color The color of the guild.
     * @return The guild instance.
     */
    public static GuildInstance getGuild(Formatting color) {
        return GUILDS.get(color);
    }

    /**
     * Gets the guild instance for the player.
     *
     * @param player The player.
     * @return The guild instance.
     */
    public static GuildInstance getGuild(PlayerEntity player) {
        return GUILDS.values().stream()
            .filter(guild -> guild.getMembers().stream()
                .map(BasicPlayerInfo::uuid)
                .toList()
                .contains(player.getUuidAsString()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Gets a guild instance by name.
     *
     * @param name The name of the guild.
     * @return The guild instance.
     */
    public static Formatting getGuildByName(String name) {
        return GUILDS.values().stream()
            .filter(guild -> guild.getName().equals(name))
            .map(GuildInstance::getColor)
            .findFirst()
            .orElse(null);
    }

    /**
     * Checks if a player is in any guild.
     *
     * @param player The player to check.
     * @return True if the player is in a guild, false otherwise.
     */
    public static boolean inGuild(PlayerEntity player) {
        return GUILDS.values().stream()
            .map(GuildInstance::getMembers)
            .flatMap(List::stream)
            .map(BasicPlayerInfo::uuid)
            .toList()
            .contains(player.getUuidAsString());
    }

    /**
     * Updates the player list.
     */
    public static void doPlayerListUpdate() {
        // Indexes 79, 59, 39, and 19 are the top of the player list.
        // Indexes 69, 49, 29, and 9 are the middle of the player list.
        // Remaining indexes are other players on the list.
        var usedIndexes = new HashSet<Integer>();
        var addedPlayers = new HashSet<PlayerEntity>();

        var simplePlayerEntries = new ArrayList<PlayerListS2CPacket.Entry>();
        var advancedPlayerEntries = new ArrayList<PlayerListS2CPacket.Entry>();

        // Insert guild headers.
        for (var entry : GUILD_ICONS.entrySet()) {
            // Get player list info.
            var color = entry.getKey();
            var iconPair = entry.getValue();
            var index = GUILD_INDEXES.get(color);

            // Get the guild instance.
            var guild = GUILDS.get(color);

            advancedPlayerEntries.add(PlayerList.fakePlayer(
                color.getName(),
                guild.getDisplayName(),
                index,
                iconPair.getLeft(),
                iconPair.getRight()
            ));

            usedIndexes.add(index);
        }

        // Add players.
        for (var entry : GUILD_RANGES.entrySet()) {
            var guild = GUILDS.get(entry.getKey());

            // Get the first 8 members, plus the owner.
            var toDisplay = new ArrayList<BasicPlayerInfo>();
            if (guild.getOwner() != null) {
                toDisplay.add(guild.getOwner());
            }
            toDisplay.addAll(guild.getMembers().stream()
                .filter(member -> !guild.isOwner(member))
                .toList());

            // Display them on the player list.
            for (var i = 0; i < Math.min(9, toDisplay.size()); i++) {
                var player = toDisplay.get(i);
                var index = entry.getValue().getRight() - i;

                var displayName = Text.literal(player.username())
                        .formatted(entry.getKey());
                if (!player.isOnline()) {
                    displayName = displayName.styled(style -> style.withStrikethrough(true));
                } else {
                    addedPlayers.add(player.toOnline());
                }

                if (player.isOnline()) {
                    var realPlayer = PlayerList.realPlayer(player.toOnline(), displayName, index);
                    simplePlayerEntries.add(realPlayer);
                    advancedPlayerEntries.add(realPlayer);
                } else {
                    advancedPlayerEntries.add(
                        PlayerList.fakePlayer(player.username(), displayName, index, PlayerList.DARK_GRAY_HEAD, PlayerList.DARK_GRAY_SIGN)
                    );
                }

                usedIndexes.add(index);
            }
        }

        // Populate remaining player entries.
        for (var i = 0; i < 80; i++) {
            // If the player entry is already a guild header, skip it.
            if (usedIndexes.contains(i)) {
                continue;
            }

            advancedPlayerEntries.add(PlayerList.fakePlayer(
                Formatting.WHITE.getName(),
                Text.empty(),
                i,
                PlayerList.DARK_GRAY_HEAD,
                PlayerList.DARK_GRAY_SIGN
            ));
        }

        // Add all players which aren't in a guild.
        for (var player : MyWellHasRunDry.getPlayers()) {
            if (addedPlayers.contains(player)) {
                continue;
            }

            var realPlayer = PlayerList.realPlayer(
                player,
                Objects.requireNonNull(player.getDisplayName()).copy()
                    .formatted(Formatting.DARK_GRAY),
                -1
            );

            simplePlayerEntries.add(realPlayer);
            advancedPlayerEntries.add(realPlayer);
        }

        // Send the player list to all online players.
        var simplePlayerList = PlayerList.create(PlayerList.NEW_ACTIONS, simplePlayerEntries);
        var advancedPlayerList = PlayerList.create(PlayerList.NEW_ACTIONS, advancedPlayerEntries);

        for (var player : MyWellHasRunDry.getPlayers()) {
            var model = Players.getModel(player);
            var networkHandler = player.networkHandler;

            // Clear existing entries.
            PlayerList.removeAllPlayers(networkHandler);

            // Send the updated player list.
            if (model.isSimplePlayerList()) {
                networkHandler.sendPacket(simplePlayerList);
            } else {
                networkHandler.sendPacket(advancedPlayerList);
            }
        }
    }
}
