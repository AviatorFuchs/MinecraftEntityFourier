package com.github.classault.fourier_series;

class DataPackNamespaceChecker {

    DataPackNamespaceChecker() {
        ;
    }

    void checkName(String raw) {
        filter(raw, (byte b) -> ((b >= '0' && b <= '9') || (b >= 'a' && b <= 'z') || (b == '-' || b == '_')));
    }

    private void filter(String raw, Filter filter) {
        byte[] target = raw.getBytes();
        for (byte s : target) {
            if (!filter.accept(s)) {
                throw new NamespaceException(raw);
            }
        }
    }

    private static class NamespaceException extends RuntimeException {
        private static final long serialVersionUID = 6871872994565201973L;

        NamespaceException(String invalidInput) {
            super("Unacceptable char in namespace or object's type: " + invalidInput);
        }
    }

    private interface Filter {
        boolean accept(byte b);
    }
}
