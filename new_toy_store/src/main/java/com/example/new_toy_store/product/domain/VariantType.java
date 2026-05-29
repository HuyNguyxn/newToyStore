package com.example.new_toy_store.product.domain;

public enum VariantType {

    DEFAULT {
        @Override
        public boolean canAddAttributes() {
            return false;
        }

        @Override
        public boolean canChangeTo(VariantType newType) {
            return false;
        }
    },

    MASTER {
        @Override
        public boolean canAddAttributes() {
            return true;
        }

        @Override
        public boolean canChangeTo(VariantType newType) {
            return newType == REGULAR;
        }
    },

    REGULAR {
        @Override
        public boolean canAddAttributes() {
            return true;
        }

        @Override
        public boolean canChangeTo(VariantType newType) {
            return newType == MASTER;
        }
    };

    public abstract boolean canAddAttributes();
    public abstract boolean canChangeTo(VariantType newType);
}