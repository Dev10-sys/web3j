package org.web3j.codegen;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.web3j.abi.DefaultFunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.StaticArray10;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.reflection.Parameterized;
import org.web3j.protocol.core.methods.response.AbiDefinition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class StructDynamicArrayDecodeTest {

    private SolidityFunctionWrapper solidityFunctionWrapper;
    private GenerationReporter generationReporter;

    @BeforeEach
    public void setUp() {
        generationReporter = mock(GenerationReporter.class);
        solidityFunctionWrapper = new SolidityFunctionWrapper(true, false, false, 20, generationReporter);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testStructGenerationWithArrays() throws Exception {
        // Define a struct with:
        // 1. address[] (dynamic)
        // 2. uint256[] (dynamic)
        // 3. uint256[10] (static)
        // 4. address[][] (nested dynamic)
        // 5. bytes (non-array dynamic)
        AbiDefinition.NamedType addressArray = new AbiDefinition.NamedType("addrArr", "address[]");
        AbiDefinition.NamedType uintArray = new AbiDefinition.NamedType("uintArr", "uint256[]");
        AbiDefinition.NamedType uintStaticArray = new AbiDefinition.NamedType("uintStatic", "uint256[10]");
        AbiDefinition.NamedType nestedAddrArray = new AbiDefinition.NamedType("nestedAddr", "address[][]");
        AbiDefinition.NamedType dynamicBytes = new AbiDefinition.NamedType("rawBytes", "bytes");

        AbiDefinition.NamedType structType = new AbiDefinition.NamedType("MyStruct", "tuple");
        structType.setInternalType("struct MyContract.MyStruct");
        structType.setComponents(Arrays.asList(addressArray, uintArray, uintStaticArray, nestedAddrArray, dynamicBytes));

        AbiDefinition function = new AbiDefinition();
        function.setType("function");
        function.setName("getStruct");
        function.setOutputs(Collections.singletonList(structType));

        // Trigger buildStructTypes
        java.lang.reflect.Method buildStructTypes = SolidityFunctionWrapper.class.getDeclaredMethod("buildStructTypes", List.class);
        buildStructTypes.setAccessible(true);
        List<TypeSpec> structs = (List<TypeSpec>) buildStructTypes.invoke(solidityFunctionWrapper, Collections.singletonList(function));

        assertEquals(1, structs.size());
        TypeSpec myStruct = structs.get(0);

        // ✔ TEST CASE 1: address[] -> annotated
        assertHasParameterizedAnnotation(myStruct, "addrArr", Address.class);

        // ✔ TEST CASE 2: uint256[] -> annotated
        assertHasParameterizedAnnotation(myStruct, "uintArr", Uint256.class);

        // ✔ TEST CASE 3: address[][] -> annotated (leaf type Address.class)
        assertHasParameterizedAnnotation(myStruct, "nestedAddr", Address.class);

        // ❌ TEST CASE 4: NEGATIVE - uint256[10] -> NOT annotated
        assertNoParameterizedAnnotation(myStruct, "uintStatic");

        // ❌ TEST CASE 5: NEGATIVE - bytes -> NOT annotated
        assertNoParameterizedAnnotation(myStruct, "rawBytes");
    }

    @Test
    public void testDecodingWithParameterizedAnnotation() {
        // Prepare encoded data for a struct MyStruct(address[])
        // address[] with 2 elements: [0x123..., 0x456...]
        String encodedAddressArray = 
                "0000000000000000000000000000000000000000000000000000000000000020" + // offset
                "0000000000000000000000000000000000000000000000000000000000000002" + // length
                "0000000000000000000000001234567890123456789012345678901234567890" + // elem 1
                "0000000000000000000000000987654321098765432109876543210987654321";   // elem 2
        
        // Struct offset
        String encodedStruct = "0000000000000000000000000000000000000000000000000000000000000020" + encodedAddressArray;

        DefaultFunctionReturnDecoder decoder = new DefaultFunctionReturnDecoder();
        
        @SuppressWarnings("unchecked")
        List<TypeReference<Type>> typeReferences = Collections.singletonList(
                (TypeReference<Type>) (TypeReference) new TypeReference<TestStruct>() {});

        List<Type> results = decoder.decodeFunctionResult(encodedStruct, typeReferences);
        
        assertNotNull(results);
        assertEquals(1, results.size());
        TestStruct decoded = (TestStruct) results.get(0);
        assertEquals(2, decoded.addrArr.getValue().size());
        assertEquals("0x1234567890123456789012345678901234567890", decoded.addrArr.getValue().get(0).getValue());
        assertEquals("0x0987654321098765432109876543210987654321", decoded.addrArr.getValue().get(1).getValue());
    }

    public static class TestStruct extends DynamicStruct {
        public DynamicArray<Address> addrArr;

        public TestStruct(@Parameterized(type = Address.class) DynamicArray<Address> addrArr) {
            super(addrArr);
            this.addrArr = addrArr;
        }
    }

    private void assertHasParameterizedAnnotation(TypeSpec typeSpec, String paramName, Class<?> expectedType) {
        MethodSpec constructor = typeSpec.methodSpecs.stream()
                .filter(m -> m.isConstructor() && m.parameters.size() > 0)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Constructor not found"));

        ParameterSpec parameter = constructor.parameters.stream()
                .filter(p -> p.name.equals(paramName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Parameter " + paramName + " not found in constructor"));

        AnnotationSpec annotation = parameter.annotations.stream()
                .filter(a -> a.type.toString().equals(Parameterized.class.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing @Parameterized annotation on " + paramName));
        
        assertTrue(annotation.members.get("type").toString().contains(expectedType.getSimpleName()),
                "Expected type " + expectedType.getSimpleName() + " but found " + annotation.members.get("type"));
    }

    private void assertNoParameterizedAnnotation(TypeSpec typeSpec, String paramName) {
        MethodSpec constructor = typeSpec.methodSpecs.stream()
                .filter(m -> m.isConstructor() && m.parameters.size() > 0)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Constructor not found"));

        ParameterSpec parameter = constructor.parameters.stream()
                .filter(p -> p.name.equals(paramName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Parameter " + paramName + " not found in constructor"));

        boolean present = parameter.annotations.stream()
                .anyMatch(a -> a.type.toString().equals(Parameterized.class.getName()));
        assertFalse(present, "Parameter " + paramName + " should NOT have @Parameterized annotation");
    }
}
