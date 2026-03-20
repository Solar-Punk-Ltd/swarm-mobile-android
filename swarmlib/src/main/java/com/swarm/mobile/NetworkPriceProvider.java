package com.swarm.mobile;

import android.util.Log;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;

public class NetworkPriceProvider {

    private static final String SWARM_CONTRACT_ADDRESS = "0x47EeF336e7fE5bED98499A4696bce8f28c1B0a8b";
    private static final String PRICE_OPERATION_NAME = "currentPrice";

    private static final BigInteger FALLBACK_PRICE = BigInteger.valueOf(60000);

    private final String rpcUrl;
    private Web3j web3j;

    public NetworkPriceProvider(String rpcUrl) {
        this.rpcUrl = rpcUrl;
        this.web3j = Web3j.build(new HttpService(rpcUrl));
    }

    public BigInteger getNetworkPrice() {

        try {
            web3j.ethBlockNumber().send();
        } catch (IOException e) {
            Log.w("NetworkPriceProvider", "Web3j connection lost, reconnecting...", e);
            web3j.shutdown();
            web3j = Web3j.build(new HttpService(rpcUrl));
            try {
                web3j.ethBlockNumber().send();
            } catch (IOException ex) {
                Log.e("NetworkPriceProvider", "Web3j still not connected after reconnect, returning fallback price", ex);
                return FALLBACK_PRICE;
            }
        }

        Function function = new Function(
                PRICE_OPERATION_NAME,
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint256>() {
                })
        );

        EthCall currentPriceResponse = queryCurrentPrice(function);

        var results = FunctionReturnDecoder.decode(currentPriceResponse.getValue(), function.getOutputParameters());

        if (!results.isEmpty()) {
            var pricePerBlock = (BigInteger) results.get(0).getValue();
            Log.d("NetworkPriceProvider", "Fetched current price from network: " + pricePerBlock);
            return pricePerBlock;
        }

        return FALLBACK_PRICE;
    }

    private EthCall queryCurrentPrice(Function function) {

        String encodedFunction = FunctionEncoder.encode(function);
        try {
            return web3j.ethCall(
                            Transaction.createEthCallTransaction(null, SWARM_CONTRACT_ADDRESS, encodedFunction),
                            DefaultBlockParameterName.LATEST)
                    .send();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
