package com.arm.mbed.cloud.sdk.lowlevel.pelionclouddevicemanagement.api;

import com.arm.mbed.cloud.sdk.lowlevel.pelionclouddevicemanagement.CollectionFormats.*;

import retrofit2.Call;
import retrofit2.http.*;

import com.arm.mbed.cloud.sdk.lowlevel.pelionclouddevicemanagement.model.ChannelMetadata;
import com.arm.mbed.cloud.sdk.lowlevel.pelionclouddevicemanagement.model.NotificationMessage;
import com.arm.mbed.cloud.sdk.lowlevel.pelionclouddevicemanagement.model.Webhook;
import com.arm.mbed.cloud.sdk.lowlevel.pelionclouddevicemanagement.model.WebsocketChannel;

public interface NotificationsApi {
    /**
     * (PREVIEW) Open the websocket. (PREVIEW) A websocket channel can have only one active connection at a time. If a
     * websocket connection for a channel exists and a new connection to the same channel is made, the connection is
     * accepted and the older connection is closed. Once the socket has been opened, it may be closed with one of the
     * following status codes. | Code | Description | |------|-------------| |**1000**|Socket closed by the client.|
     * |**1001**|Going away. Set when another socket was opened on the used channel, or if the channel was deleted with
     * a REST call, or if the server is shutting down.| |**1006**|Abnormal loss of connection. This code is never set by
     * the service.| |**1008**|Policy violation. Set when the API key is missing or invalid.| |**1011**|Unexpected
     * condition. Socket is closed with this status at an attempt to open a socket to an unexisting channel (without a
     * prior REST PUT). This code is also used to indicate closing socket for any other unexpected condition in the
     * server.| **Example:** &#x60;&#x60;&#x60; curl -X GET
     * https://api.us-east-1.mbedcloud.com/v2/notification/websocket-connect \\ -H \&quot;Authorization:Bearer
     * {apikey}\&quot; \\ -H \&quot;Connection:upgrade\&quot; \\ -H \&quot;Upgrade:websocket\&quot; \\ -H
     * \&quot;Sec-WebSocket-Version: 13\&quot; \\ -H \&quot;Sec-WebSocket-Key: {base64nonce}\&quot; \\ -N -I
     * &#x60;&#x60;&#x60;
     * 
     * @param connection
     *            (required)
     * @param upgrade
     *            (required)
     * @param origin
     *            Originating host of the request. (required)
     * @param secWebSocketVersion
     *            WebSocket version. must be 13. (required)
     * @param secWebSocketKey
     *            The value of this header field must be a nonce consisting of a randomly selected 16-byte value that
     *            has been base64-encoded (see Section 4 of [RFC4648]). The nonce must be selected randomly for each
     *            connection. (required)
     * @param secWebSocketProtocol
     *            API key or user token must be present in the &#x60;Sec-WebSocket-Protocol&#x60; header **if
     *            Authorization header cannot be provided**:
     *            &#x60;Sec-WebSocket-Protocol:\&quot;wss,pelion_ak_{api_key}\&quot;&#x60;. Refer to the notification
     *            service documentation for examples. (optional)
     * @return Call&lt;Void&gt;
     */
    @GET("v2/notification/websocket-connect")
    Call<Void> connectWebsocket(@retrofit2.http.Header("Connection") String connection,
                                @retrofit2.http.Header("Upgrade") String upgrade,
                                @retrofit2.http.Header("Origin") String origin,
                                @retrofit2.http.Header("Sec-WebSocket-Version") Integer secWebSocketVersion,
                                @retrofit2.http.Header("Sec-WebSocket-Key") String secWebSocketKey,
                                @retrofit2.http.Header("Sec-WebSocket-Protocol") String secWebSocketProtocol);

    /**
     * Delete notification Long Poll channel. Delete a notification Long Poll channel. This is required to change the
     * channel from Long Poll to another type. Do not make a GET &#x60;/v2/notification/pull&#x60; call for two minutes
     * after deleting the channel, because it can implicitly recreate the pull channel. You can also have some random
     * responses with payload or 410 GONE with \&quot;CHANNEL_DELETED\&quot; as a payload or 200/204 until the old
     * channel is purged. **Example:** &#x60;&#x60;&#x60; curl -X DELETE
     * https://api.us-east-1.mbedcloud.com/v2/notification/pull \\ -H &#39;Authorization: Bearer &lt;api_key&gt;&#39;
     * &#x60;&#x60;&#x60;
     * 
     * @return Call&lt;Void&gt;
     */
    @DELETE("v2/notification/pull")
    Call<Void> deleteLongPollChannel();

    /**
     * (PREVIEW) Delete websocket channel. (PREVIEW) Delete a notification websocket channel bound to the API key. This
     * is required to change the channel from websocket to another type. **Example:** &#x60;&#x60;&#x60; curl -X DELETE
     * https://api.us-east-1.mbedcloud.com/v2/notification/websocket \\ -H &#39;Authorization: Bearer
     * &lt;api_key&gt;&#39; &#x60;&#x60;&#x60;
     * 
     * @return Call&lt;Void&gt;
     */
    @DELETE("v2/notification/websocket")
    Call<Void> deleteWebsocket();

    /**
     * Delete callback URL. Deletes the callback URL. **Example:** &#x60;&#x60;&#x60; curl -X DELETE
     * https://api.us-east-1.mbedcloud.com/v2/notification/callback \\ -H &#39;Authorization: Bearer
     * &lt;api_key&gt;&#39; &#x60;&#x60;&#x60;
     * 
     * @return Call&lt;Void&gt;
     */
    @DELETE("v2/notification/callback")
    Call<Void> deregisterWebhook();

    /**
     * Get channel metadata. Get channel delivery mechanism. **Example:** curl -X GET
     * https://api.us-east-1.mbedcloud.com/v2/notification/channel \\ -H &#39;Authorization: Bearer &lt;api_key&gt;&#39;
     * \\
     * 
     * @return Call&lt;ChannelMetadata&gt;
     */
    @GET("v2/notification/channel")
    Call<ChannelMetadata> getChannelMetadata();

    /**
     * Check callback URL. Shows the current callback URL if it exists. **Example:** &#x60;&#x60;&#x60; curl -X GET
     * https://api.us-east-1.mbedcloud.com/v2/notification/callback \\ -H &#39;Authorization: Bearer
     * &lt;api_key&gt;&#39; &#x60;&#x60;&#x60;
     * 
     * @return Call&lt;Webhook&gt;
     */
    @GET("v2/notification/callback")
    Call<Webhook> getWebhook();

    /**
     * (PREVIEW) Get websocket channel information. (PREVIEW) Returns 200 with websocket connection status, if websocket
     * channel exists. **Example:** &#x60;&#x60;&#x60; curl -X GET
     * https://api.us-east-1.mbedcloud.com/v2/notification/websocket \\ -H &#39;Authorization: Bearer
     * &lt;api_key&gt;&#39; &#x60;&#x60;&#x60;
     * 
     * @return Call&lt;WebsocketChannel&gt;
     */
    @GET("v2/notification/websocket")
    Call<WebsocketChannel> getWebsocket();

    /**
     * Get notifications using Long Poll In this case, notifications are delivered through HTTP long poll requests. The
     * HTTP request is kept open until one or more event notifications are delivered to the client, or the request times
     * out (response code 204). In both cases, the client should open a new polling connection after the previous one
     * closes. Only a single long polling connection per API key can be ongoing at any given time. You must have a
     * persistent connection (Connection keep-alive header in the request) to avoid excess TLS handshakes. The pull
     * channel is implicitly created by the first GET call to &#x60;/v2/notification/pull&#x60;. It refreshes on each
     * GET call. If the channel is not polled for a long time (10 minutes), it expires and is deleted. This means that
     * no notifications will stay in the queue between polls. A channel can be also be deleted explicitly with a DELETE
     * call. **Note:** If you cannot have a public-facing callback URL, for example, when developing on your local
     * machine, you can use long polling to check for new messages. However, **long polling is deprecated** and will
     * likely be replaced in the future. It is meant only for experimentation, not commercial use. The proper method to
     * receive notifications is a **notification callback**. There can only be one notification channel per API key in
     * Device Management Connect. If a notification channel of other type already exists for the API key, delete it
     * before creating a long poll notification channel. **Example:** &#x60;&#x60;&#x60; curl -X GET
     * https://api.us-east-1.mbedcloud.com/v2/notification/pull \\ -H &#39;Authorization: Bearer &lt;api_key&gt;&#39;
     * &#x60;&#x60;&#x60;
     * 
     * @return Call&lt;NotificationMessage&gt;
     */
    @GET("v2/notification/pull")
    Call<NotificationMessage> longPollNotifications();

    /**
     * Register a callback URL. Register a URL to which the server should deliver notifications of the subscribed
     * resource changes. To push notifications, you must place subscriptions. The maximum length of the URL, header
     * keys, and values, all combined, is 400 characters. Notifications are delivered as PUT requests to the HTTP
     * server, defined by the client with a subscription server message. The given URL should be accessible and respond
     * to the PUT request with a response code of 200 or 204. Device Management Connect tests the callback URL with an
     * empty payload when the URL is registered. Callback implementation does not support URL redirection. For more
     * information on notification messages, see [NotificationMessage](#NotificationMessage). **Optional headers in a
     * callback message:** You can set optional headers to a callback in a **Webhook** object. Device Management Connect
     * includes the header and key pairs in the notification messages send them to callback URL. The callback URLs and
     * headers are API key-specific. One possible use for additional headers is checking the origin of a PUT request, as
     * well as distinguishing the application (API key) to which the notification belongs. **Note**: Only one callback
     * URL per API key can be active. If you register a new URL while another one is active, it replaces the active one.
     * There can be only one notification channel at a time. If another type of channel is already present, you need to
     * delete it before setting the callback URL. **Expiration of a callback URL:** A callback can expire when Device
     * Management cannot deliver a notification due to a connection timeout or an error response (4xx or 5xx). After
     * each delivery failure, Device Management sets an exponential back off time and makes a retry attempt after that.
     * The first retry delay is 1 second, then 2s, 4s, 8s, up to maximum delay of two minutes. The callback URL is
     * removed if all retries fail within 24 hours. More about [notification sending
     * logic](../integrate-web-app/event-notification.html#notification-sending-logic). **Supported callback URL
     * protocols:** Currently, only HTTP and HTTPS protocols are supported. **HTTPS callback URLs:** When delivering a
     * notification to an HTTPS based callback URL, Device Management Connect presents a valid client certificate to
     * identify itself. The certificate is signed by a trusted certificate authorithy (GlobalSign) with a Common Name
     * (CN) set to notifications.mbedcloud.com. **Example:** This example command shows how to set your callback URL and
     * API key. It also sets an optional header authorization. When Device Management Connect calls your callback URL,
     * the call contains the authorization header with the defined value. &#x60;&#x60;&#x60; curl -X PUT
     * https://api.us-east-1.mbedcloud.com/v2/notification/callback \\ -H &#39;Authorization: Bearer
     * &lt;api_key&gt;&#39; \\ -H &#39;content-type: application/json&#39; \\ -d &#39;{ \&quot;url\&quot;:
     * \&quot;{callback-url}\&quot;, \&quot;headers\&quot;: {\&quot;authorization\&quot; :
     * \&quot;f4b93d6e-4652-4874-82e4-41a3ced0cd56\&quot;} }&#39; &#x60;&#x60;&#x60;
     * 
     * @param webhook
     *            A JSON object that contains the optional headers and URL where notifications are sent. (required)
     * @return Call&lt;Void&gt;
     */
    @Headers({ "Content-Type:application/json" })
    @PUT("v2/notification/callback")
    Call<Void> registerWebhook(@retrofit2.http.Body Webhook webhook);

    /**
     * (PREVIEW) Register a websocket channel. (PREVIEW) Register (or update) a channel using websocket connection to
     * deliver notifications. The websocket channel should be opened by client using
     * &#x60;/v2/notification/websocket-connect&#x60; endpoint. To get notifications pushed, you must place
     * subscriptions. For more information on notification messages, see [NotificationMessage](#NotificationMessage). A
     * websocket channel can have only one active websocket connection at a time. If a websocket connection for a
     * channel exists and a new connection to the same channel is made, the connection is accepted and the older
     * connection is closed. **Expiration of a websocket:** A websocket channel is expired if the channel does not have
     * an opened websocket connection for a 24-hour period. Channel expiration means the channel is deleted and any
     * undelivered notifications stored in its internal queue is removed. As long as the channel has an opened websocket
     * connection or time between successive websocket connections is less than 24 hours, the channel is considered
     * active, notifications are stored in its internal queue and delivered when a websocket connection is active. A
     * channel can be also deleted explicitly with a DELETE call. More about [notification sending
     * logic](../integrate-web-app/event-notification.html#notification-sending-logic). **Example:** &#x60;&#x60;&#x60;
     * curl -X PUT https://api.us-east-1.mbedcloud.com/v2/notification/websocket \\ -H &#39;Authorization: Bearer
     * &lt;api_key&gt;&#39; &#x60;&#x60;&#x60;
     * 
     * @return Call&lt;WebsocketChannel&gt;
     */
    @Headers({ "Content-Type:application/json" })
    @PUT("v2/notification/websocket")
    Call<WebsocketChannel> registerWebsocket();

}
