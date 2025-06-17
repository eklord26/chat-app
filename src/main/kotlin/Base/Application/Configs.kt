package Base.Application

import io.ktor.server.config.*

public class Configs (var entity: String) {

    private var config: MutableMap<String, String> = mutableMapOf()

    public fun getConfig(): Map<String, String> {
        ConfigLoader.load().config(entity).toMap().forEach { t, u -> config.put(t.toString(), u.toString()) }
        return this.config
    }
}

