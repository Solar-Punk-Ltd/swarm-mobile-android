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
        ensureWeb3Connected();

        Function web3QueryFunction = new Function(
                PRICE_OPERATION_NAME,
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint256>() {
                })
        );

        try {
            EthCall currentNetworkPriceResponse = queryNetworkPrice(web3QueryFunction);

            var results = FunctionReturnDecoder.decode(currentNetworkPriceResponse.getValue(), web3QueryFunction.getOutputParameters());

            if (!results.isEmpty()) {
                var pricePerBlock = (BigInteger) results.get(0).getValue();
                Log.d("NetworkPriceProvider", "Fetched current price from network: " + pricePerBlock);
                return pricePerBlock;
            }
        } catch (Exception e){
            Log.e("NetworkPriceProvider", "Failed to query current price from Web3: " + e.getMessage(), e);
        }

        return FALLBACK_PRICE;
    }

    private void ensureWeb3Connected() {
        if (!web3jIsConnected()) {
            Log.w("NetworkPriceProvider", "Web3j connection lost, attempting to reconnect...");
            reconnectWithRetry();
            Log.w("NetworkPriceProvider", "Web3j connected.");
        }
    }


    private void reconnectWithRetry() {
        var maxAttempts = 5;
        for(int attempt = 0; attempt <= maxAttempts; attempt++) {
            try {
                reconnect();
                if (web3jIsConnected()) {
                    Log.d("NetworkPriceProvider", "Successfully reconnected to Web3j on attempt " + attempt);
                    return;
                }

                Thread.sleep(2000L * attempt);
            } catch (InterruptedException e) {
                Log.e("NetworkPriceProvider", "Reconnect interrupted on " + attempt + ": " + e.getMessage());
                Thread.currentThread().interrupt();
            }
            catch (Exception e) {
                Log.e("NetworkPriceProvider", "Reconnection attempt " + attempt + " failed: " + e.getMessage());
            }
        }

        String message = "Failed to reconnect to Web3 after " + maxAttempts + " attempts";
        Log.e("NetworkPriceProvider", message);

        throw new RuntimeException(message);
    }

    private boolean web3jIsConnected() {
        try {
            web3j.ethBlockNumber().send();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void reconnect() {
        web3j.shutdown();
        web3j = Web3j.build(new HttpService(rpcUrl));
    }

    private EthCall queryNetworkPrice(Function function) throws IOException {
        String encodedFunction = FunctionEncoder.encode(function);
            return web3j.ethCall(
                            Transaction.createEthCallTransaction(null, SWARM_CONTRACT_ADDRESS, encodedFunction),
                            DefaultBlockParameterName.LATEST)
                    .send();
    }
}
