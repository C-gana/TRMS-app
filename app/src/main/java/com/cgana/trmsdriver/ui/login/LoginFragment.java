package com.cgana.trmsdriver.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.cgana.trmsdriver.R;
import com.cgana.trmsdriver.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.loadingContainer.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!isLoading);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (TextUtils.isEmpty(errorMsg)) {
                binding.errorContainer.setVisibility(View.GONE);
            } else {
                binding.errorContainer.setVisibility(View.VISIBLE);
                binding.tvError.setText(errorMsg);
            }
        });

        viewModel.getNavigateHome().observe(getViewLifecycleOwner(), go -> {
            if (Boolean.TRUE.equals(go)) {
                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_homeFragment);
                viewModel.onNavigated();
            }
        });
    }

    private void attemptLogin() {
        String phone = binding.etPhoneNumber.getText() != null ? binding.etPhoneNumber.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";
        boolean remember = binding.cbRememberMe.isChecked();

        if (phone.isEmpty()) {
            Toast.makeText(requireContext(), "Phone required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(requireContext(), "Password required", Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.login(phone, password, remember);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

