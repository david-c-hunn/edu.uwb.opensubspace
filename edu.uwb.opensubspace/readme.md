

## Manually importing dependecies for CLON algorithm.  
```
# get antwerp jar, this will fail to compile due to a missing dependency,
# but the jar shows up.
mvn dependency:get -Dartifact=be.uantweepen.adrem:carti-model:0.1

# copy the jar to the library folder
cp /Users/dave/.m2/repository/be/uantwerpen/adrem/carti-model/0.1/carti-model-0.1.jar library/

# get google's guava library
mvn dependency:get -Dartifact=com.google.guava:guava:18.0

# copy it the jar to the library folder
cp $HOME/.m2/repository/com/googe/guava/18.0/guava-18.0.jar library/
```

mvn dependency:get -Dartifact=be.uantwerpen.adrem:fim-model:0.1
cp /Users/dave/.m2/repository/be/uantwerpen/adrem/fim-model/0.1/fim-model-0.1.jar library/