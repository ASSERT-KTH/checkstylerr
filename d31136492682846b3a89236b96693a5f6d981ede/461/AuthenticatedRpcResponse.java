package io.openems.common.jsonrpc.response;

import java.util.UUID;

import com.google.gson.JsonObject;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.jsonrpc.base.JsonrpcResponseSuccess;
import io.openems.common.utils.JsonUtils;

/**
 * Represents a JSON-RPC Response for 'authenticatedRpc'.
 * 
 * <pre>
 * {
 *   "jsonrpc": "2.0",
 *   "id": "UUID",
 *   "result": {
 *     "payload": {@link JsonrpcResponseSuccess}
 *   }
 * }
 * </pre>
 */
public class AuthenticatedRpcResponse extends JsonrpcResponseSuccess {

	public static AuthenticatedRpcResponse from(JsonrpcResponseSuccess r) throws OpenemsNamedException {
		JsonObject p = r.getResult();
		JsonrpcResponseSuccess payload = JsonrpcResponseSuccess.from(JsonUtils.getAsJsonObject(p, "payload"));
		return new AuthenticatedRpcResponse(r.getId(), payload);
	}

	private final JsonrpcResponseSuccess payload;

	public AuthenticatedRpcResponse(UUID id, JsonrpcResponseSuccess payload) {
		super(id);
		this.payload = payload;
	}

	public JsonrpcResponseSuccess getPayload() {
		return payload;
	}

	@Override
	public JsonObject getResult() {
		return JsonUtils.buildJsonObject() //
				.add("payload", this.payload.toJsonObject()) //
				.build();
	}

}
