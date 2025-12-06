package com.app.shambabora.modules.marketplace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaymentCallbackRequest {
    @JsonProperty("Body")
    private CallbackBody body;

    @Data
    public static class CallbackBody {
        @JsonProperty("stkCallback")
        private StkCallback stkCallback;

        @Data
        public static class StkCallback {
            @JsonProperty("MerchantRequestID")
            private String merchantRequestId;

            @JsonProperty("CheckoutRequestID")
            private String checkoutRequestId;

            @JsonProperty("ResultCode")
            private int resultCode;

            @JsonProperty("ResultDesc")
            private String resultDesc;

            @JsonProperty("CallbackMetadata")
            private CallbackMetadata callbackMetadata;

            @Data
            public static class CallbackMetadata {
                @JsonProperty("Item")
                private java.util.List<Item> items;

                @Data
                public static class Item {
                    @JsonProperty("Name")
                    private String name;

                    @JsonProperty("Value")
                    private Object value;
                }
            }
        }
    }
}
