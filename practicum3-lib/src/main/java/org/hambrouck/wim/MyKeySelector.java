package org.hambrouck.wim;

import javax.xml.crypto.*;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyName;
import java.security.Key;
import java.util.List;

/**
 * Created by Wim Hambrouck
 */
public class MyKeySelector extends KeySelector implements KeySelectorResult {

    private final Key publicKey;

    MyKeySelector(Key publicKey)
    {
        this.publicKey = publicKey;
    }

    @Override
    public KeySelectorResult select(KeyInfo keyInfo, Purpose purpose, AlgorithmMethod method, XMLCryptoContext context) throws KeySelectorException {
        List<XMLStructure> keyInfoContent = keyInfo.getContent();
        for (XMLStructure keyInfoStructure : keyInfoContent) {
            if (keyInfoStructure instanceof KeyName) {
                KeyName keyName = (KeyName) keyInfoStructure;
                if (IntegriteitsModule.KEY_NAME.equals(keyName.getName())) {
                    return this;
                }
            }
        }
        return null;
    }

    @Override
    public Key getKey() {
        return publicKey;
    }
}
